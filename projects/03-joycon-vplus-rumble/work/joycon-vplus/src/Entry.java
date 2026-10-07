package local.switchfix;
import android.view.InputDevice;
import android.view.MotionEvent;
import de.robv.android.xposed.*;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
public class Entry implements IXposedHookLoadPackage {
 private static boolean logged=false;
 public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lp) throws Throwable {
  if (!lp.packageName.equals("com.limelight.qiin")) return;
  try {
   Class<?> c=XposedHelpers.findClass("com.limelight.binding.input.haptics.ControllerHapticsCoordinator",lp.classLoader);
   java.util.Set<XC_MethodHook.Unhook> hooks=XposedBridge.hookAllMethods(c,"hasRumbleCapability",new XC_MethodHook(){
    protected void afterHookedMethod(MethodHookParam p) throws Throwable {
     if(p.hasThrowable() || Boolean.TRUE.equals(p.getResult()) || p.args.length!=1) return;
     Object ctx=p.args[0];
     if(ctx==null || !ctx.getClass().getSimpleName().equals("InputDeviceContext")) return;
     try {
      if(XposedHelpers.getBooleanField(ctx,"external")) return;
      InputDevice dev=(InputDevice)XposedHelpers.getObjectField(ctx,"inputDevice");
      if(dev==null || dev.getVendorId()!=0x057e || dev.getProductId()!=0x2008 || !dev.getName().contains("Combined Joy-Cons")) return;
      Object handler=XposedHelpers.getObjectField(p.thisObject,"handler");
      Object prefs=XposedHelpers.getObjectField(handler,"prefConfig");
      if(XposedHelpers.getBooleanField(prefs,"multiController") && !XposedHelpers.getBooleanField(ctx,"assignedControllerNumber")) return;
      if(dev.getMotionRange(MotionEvent.AXIS_X,InputDevice.SOURCE_JOYSTICK)==null || dev.getMotionRange(MotionEvent.AXIS_Y,InputDevice.SOURCE_JOYSTICK)==null) return;
      if(XposedHelpers.getObjectField(ctx,"vibratorManager")==null && XposedHelpers.getObjectField(ctx,"vibrator")==null) return;
      p.setResult(true);
      if(!logged){logged=true; XposedBridge.log("SwitchJoyConFix: enabled rumble for built-in Combined Joy-Cons");}
     } catch(Throwable e){if(!logged){logged=true;XposedBridge.log("SwitchJoyConFix: incompatible layout, no change: "+e);}}
    }
   });
   XposedBridge.log("SwitchJoyConFix: installed "+hooks.size()+" capability hooks");
  }catch(Throwable e){XposedBridge.log("SwitchJoyConFix: hook unavailable, no change: "+e);}
 }
}
