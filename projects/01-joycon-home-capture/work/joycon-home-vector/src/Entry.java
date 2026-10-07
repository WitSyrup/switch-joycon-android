package local.joycon.home;

import android.os.*;
import android.view.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import de.robv.android.xposed.*;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/** Android 15 system-server policy adapter. Never changes keylayout files. */
public final class Entry implements IXposedHookLoadPackage {
 private static final String MARKER=" [JoyConHome]";
 private boolean appHooked, systemProcess;
 private Object wms, input;
 private Handler sender;
 private final Map<Integer,Route> routes=new HashMap<>();
 private final Map<Integer,Route> captures=new HashMap<>();
 private IBinder joycond;
 private final Map<Long,Route> forwarded=new ConcurrentHashMap<>();
 private boolean ready, errorLogged;
 private static final class Route {
  final long down; final int uid; final String pkg; final boolean mapped;
  Route(long d,int u,String p,boolean m){down=d;uid=u;pkg=p;mapped=m;}
 }
 private static void log(String s){XposedBridge.log("JoyConHome: "+s);}
 private static boolean joycon(KeyEvent e){
  InputDevice d=e.getDevice();
  if(d==null || d.getVendorId()!=0x057e || (e.getScanCode()!=316&&e.getScanCode()!=309))return false;
  int p=d.getProductId();String n=d.getName();
  return (p==0x2006||p==0x2007||p==0x2008)&&n!=null&&(n.contains("Joy-Con")||n.contains("JoyCon"));
 }
 public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lp){
  if(!"android".equals(lp.packageName)){
   if(!systemProcess&&lp.isFirstApplication&&!"local.joycon.home".equals(lp.packageName))markScopedWindows(lp);
   return;
  }
  systemProcess=true;
  try {
   Class<?> policy=XposedHelpers.findClass("com.android.server.policy.PhoneWindowManager",lp.classLoader);
   XposedBridge.hookAllMethods(policy,"init",new XC_MethodHook(){protected void afterHookedMethod(MethodHookParam p){initialize(p.thisObject);}});
   // Capture is MEDIA_PLAY_PAUSE in the vendor layout. Intercept before policy
   // dispatches it to a media session, even when the focused app doesn't take it.
   XposedHelpers.findAndHookMethod(policy,"interceptKeyBeforeQueueing",KeyEvent.class,int.class,new XC_MethodHook(){
    protected void beforeHookedMethod(MethodHookParam p){
     if(!ready)return;
     KeyEvent e=(KeyEvent)p.args[0];
     if(e.getScanCode()!=309||!joycon(e))return;
     try{capture(p,e);}catch(Throwable t){log("Capture unchanged after error: "+t);}
    }
   });
   XposedHelpers.findAndHookMethod(policy,"interceptKeyBeforeDispatching",IBinder.class,KeyEvent.class,int.class,new XC_MethodHook(){
    protected void beforeHookedMethod(MethodHookParam p){
     if(!ready)return;
     KeyEvent e=(KeyEvent)p.args[1];
     if(isForwarded(e)){p.setResult(0L);return;}
     if(e.getKeyCode()!=KeyEvent.KEYCODE_HOME || e.getScanCode()!=316 || !joycon(e))return;
     try{route(p,e);}catch(Throwable t){if(!errorLogged){errorLogged=true;log("Home unchanged after error: "+t);}}
    }
   });
   XposedHelpers.findAndHookMethod(policy,"interceptUnhandledKey",KeyEvent.class,IBinder.class,new XC_MethodHook(){
    protected void beforeHookedMethod(MethodHookParam p){if(isForwarded((KeyEvent)p.args[0]))p.setResult(true);}
   });
   log("policy hooks installed");
  }catch(Throwable t){log("unsupported policy; leaving stock behavior: "+t);}
 }
 private synchronized void markScopedWindows(XC_LoadPackage.LoadPackageParam lp){
  if(appHooked)return;
  try{
   Class<?> root=XposedHelpers.findClass("android.view.ViewRootImpl",lp.classLoader);
   XC_MethodHook mark=new XC_MethodHook(){
    protected void beforeHookedMethod(MethodHookParam p){
     for(int i=0;i<p.args.length;i++)if(p.args[i] instanceof WindowManager.LayoutParams){
      WindowManager.LayoutParams original=(WindowManager.LayoutParams)p.args[i];
      String title=original.getTitle().toString();
      if(!title.endsWith(MARKER)){
       // Copy parameters, leaving the app's own attributes and title untouched.
       // This diagnostic window-title suffix crosses the existing WindowManager
       // IPC; no service, preferences, permission, or scope database is needed.
       WindowManager.LayoutParams copy=new WindowManager.LayoutParams();
       copy.copyFrom(original);copy.setTitle(title+MARKER);p.args[i]=copy;
      }
     }
    }
   };
   int a=XposedBridge.hookAllMethods(root,"setView",mark).size();
   int b=XposedBridge.hookAllMethods(root,"setLayoutParams",mark).size();
   appHooked=true;log("Vector scope active: "+lp.packageName+" (window hooks "+a+"/"+b+")");
  }catch(Throwable e){log("app scope unavailable; stock Home preserved: "+e);}
 }
 private synchronized void initialize(Object policy){
  if(ready)return;
  try{
   wms=XposedHelpers.getObjectField(policy,"mWindowManagerFuncs");
   XposedHelpers.getObjectField(wms,"mInputToWindowMap");
   XposedHelpers.getObjectField(wms,"mGlobalLock");
   Object service=XposedHelpers.getObjectField(wms,"mInputManager");
   input=XposedHelpers.getObjectField(service,"mNative");
   XposedHelpers.findMethodExact(input.getClass(),"injectInputEvent",InputEvent.class,boolean.class,int.class,int.class,int.class,int.class);
   HandlerThread t=new HandlerThread("JoyConHomeSender");t.start();sender=new Handler(t.getLooper());ready=true;
   log("ready; Vector-scoped Home + Capture/F12 routing active v2.1");
  }catch(Throwable e){log("initialization skipped: "+e);}
 }
 private boolean isForwarded(KeyEvent e){
  if(e.getKeyCode()!=KeyEvent.KEYCODE_BUTTON_MODE||e.getScanCode()!=316)return false;
  // Match the original gesture time, including fallback processing.
  return forwarded.containsKey(e.getDownTime());
 }
 private Route captureTarget(Object policy,KeyEvent e){
  if(Boolean.TRUE.equals(XposedHelpers.callMethod(policy,"keyguardOn")))return new Route(e.getDownTime(),-1,"",false);
  Object lock=XposedHelpers.getObjectField(wms,"mGlobalLock");
  synchronized(lock){
   Object win=XposedHelpers.callMethod(wms,"getFocusedWindowLocked");
   if(win!=null){
    WindowManager.LayoutParams attrs=(WindowManager.LayoutParams)XposedHelpers.getObjectField(win,"mAttrs");
    if(attrs.getTitle().toString().endsWith(MARKER))return new Route(e.getDownTime(),
      (Integer)XposedHelpers.callMethod(win,"getOwningUid"),(String)XposedHelpers.callMethod(win,"getOwningPackage"),true);
   }
  }
  return new Route(e.getDownTime(),-1,"",false);
 }
 private void mouseOff() throws Exception {
  if(joycond==null||!joycond.isBinderAlive())joycond=(IBinder)XposedHelpers.callStaticMethod(
    XposedHelpers.findClass("android.os.ServiceManager",null),"checkService","android.hardware.nintendo.joycond.IJoycond/default");
  if(joycond==null)throw new IllegalStateException("Joycond service unavailable");
  Parcel data=Parcel.obtain(),reply=Parcel.obtain();
  try{
   data.writeInterfaceToken("android.hardware.nintendo.joycond.IJoycond");data.writeInt(0);
   // Stable vendor AIDL v1: transaction 8 is setRsmouse(boolean).
   if(!joycond.transact(8,data,reply,0))throw new IllegalStateException("setRsmouse unsupported");
   reply.readException();
  }finally{reply.recycle();data.recycle();}
 }
 private void capture(XC_MethodHook.MethodHookParam p,KeyEvent e)throws Exception {
  int device=e.getDeviceId();Route r=captures.get(device);
  if(e.getAction()==KeyEvent.ACTION_DOWN&&e.getRepeatCount()==0){r=captureTarget(p.thisObject,e);captures.put(device,r);}
  if(r==null||r.down!=e.getDownTime())return;
  if(e.getAction()==KeyEvent.ACTION_UP)captures.remove(device);
  if(!r.mapped)return;
  // The native service has already toggled rsmouse before emitting this event.
  // Reset synchronously at the earliest Java policy boundary. This leaves the
  // vendor binary and all non-scoped behavior intact; it isn't a native veto.
  if(e.getAction()==KeyEvent.ACTION_DOWN){
   try{mouseOff();}
   catch(Exception failure){captures.put(device,new Route(r.down,-1,"",false));throw failure;}
  }
  final Route target=r;
  // A virtual keyboard identity is intentional: retaining the controller ID
  // allows Moonlight's controller-specific scan-code handling to eat F12.
  final KeyEvent key=new KeyEvent(e.getDownTime(),e.getEventTime(),e.getAction(),KeyEvent.KEYCODE_F12,
    e.getRepeatCount(),0,KeyCharacterMap.VIRTUAL_KEYBOARD,88,0,InputDevice.SOURCE_KEYBOARD);
  try{XposedHelpers.callMethod(key,"setDisplayId",XposedHelpers.callMethod(e,"getDisplayId"));}catch(Throwable ignored){}
  sender.post(()->{
   try{
    int result=(Integer)XposedHelpers.callMethod(input,"injectInputEvent",key,true,target.uid,1,1000,0x4c000000);
    log((result==0?"sent ":"not delivered ("+result+") ")+(key.getAction()==0?"DOWN":"UP")+" F12 -> "+target.pkg+" mouse=off");
   }catch(Throwable t){log("F12 delivery failed: "+t);}
  });
  p.setResult(0);
 }
 private void route(XC_MethodHook.MethodHookParam p,KeyEvent e){
  final int device=e.getDeviceId();
  Route r=routes.get(device);
  if(e.getAction()==KeyEvent.ACTION_DOWN&&e.getRepeatCount()==0){
   r=new Route(e.getDownTime(),-1,"",false);
   if(!Boolean.TRUE.equals(XposedHelpers.callMethod(p.thisObject,"keyguardOn"))){
    Object lock=XposedHelpers.getObjectField(wms,"mGlobalLock");
    String pkg=null;int uid=-1;boolean scoped=false;
    synchronized(lock){
     Map<?,?> windows=(Map<?,?>)XposedHelpers.getObjectField(wms,"mInputToWindowMap");
     Object win=windows.get(p.args[0]);
     if(win!=null){
      pkg=(String)XposedHelpers.callMethod(win,"getOwningPackage");uid=(Integer)XposedHelpers.callMethod(win,"getOwningUid");
      WindowManager.LayoutParams attrs=(WindowManager.LayoutParams)XposedHelpers.getObjectField(win,"mAttrs");
      scoped=attrs.getTitle().toString().endsWith(MARKER);
     }
    }
    if(pkg!=null&&scoped)r=new Route(e.getDownTime(),uid,pkg,true);
   }
   routes.put(device,r);
  }
  if(r==null||r.down!=e.getDownTime())return;
  if(e.getAction()==KeyEvent.ACTION_UP)routes.remove(device);
  if(!r.mapped)return;
  final Route target=r;
  if(forwarded.size()>32){long before=SystemClock.uptimeMillis()-60000;for(Long time:forwarded.keySet())if(time<before)forwarded.remove(time);}
  forwarded.put(r.down,r);
  final KeyEvent guide=new KeyEvent(e.getDownTime(),e.getEventTime(),e.getAction(),KeyEvent.KEYCODE_BUTTON_MODE,
    e.getRepeatCount(),e.getMetaState(),device,e.getScanCode(),e.getFlags(),e.getSource());
  try{XposedHelpers.callMethod(guide,"setDisplayId",XposedHelpers.callMethod(e,"getDisplayId"));}catch(Throwable ignored){}
  // Never block the input dispatcher waiting for its own injected event.
  sender.post(()->{
   try{
    // The system policy has already filtered this physical event. FILTERED
    // preserves its Joy-Con ID instead of turning it into a virtual keyboard.
    // Keep UID-targeting; a focus change must never send the key to another app.
    // FILTERED | DISABLE_KEY_REPEAT | PASS_TO_USER, wait-for-result off dispatcher.
    int result=(Integer)XposedHelpers.callMethod(input,"injectInputEvent",guide,true,target.uid,1,1000,0x4c000000);
    log((result==0?"sent ":"not delivered ("+result+") ")+ (guide.getAction()==0?"DOWN":"UP")+" Guide -> "+target.pkg+" device="+device);
   }catch(Throwable t){log("delivery failed: "+t);}
  });
  p.setResult(-1L);
 }
}
