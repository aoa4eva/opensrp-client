package org.ei.opensrp.indonesia_demo;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;


import org.ei.opensrp.Context;
import org.ei.opensrp.indonesia_demo.anc.NativeKIANCSmartRegisterActivity;
import org.ei.opensrp.indonesia_demo.child.NativeKIAnakSmartRegisterActivity;
import org.ei.opensrp.indonesia_demo.kartu_ibu.NativeKISmartRegisterActivity;
import org.ei.opensrp.indonesia_demo.kb.NativeKBSmartRegisterActivity;
import org.ei.opensrp.indonesia_demo.pnc.NativeKIPNCSmartRegisterActivity;
import org.ei.opensrp.view.controller.ANMController;
import org.json.JSONObject;


import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class NavigationControllerINA extends org.ei.opensrp.view.controller.NavigationController {
    private Activity activity;
    private ANMController anmController;
    private Context context;

    public NavigationControllerINA(Activity activity, ANMController anmController, Context context) {
        super(activity,anmController);
        this.activity = activity;
        this.anmController = anmController;
        this.context=context;
    }
    @Override
    public void startECSmartRegistry() {
           activity.startActivity(new Intent(activity, NativeKISmartRegisterActivity.class));
        ///  activity.startActivity(new Intent(activity, HouseHoldSmartRegisterActivity.class));
        SharedPreferences sharedPreferences = getDefaultSharedPreferences(this.activity);

        if(sharedPreferences.getBoolean("firstlauch",true)) {
            sharedPreferences.edit().putBoolean("firstlauch",false).commit();
            //     activity.startActivity(new Intent(activity, tutorialCircleViewFlow.class));
        }

    }
    @Override
    public void startFPSmartRegistry() {
           activity.startActivity(new Intent(activity, NativeKBSmartRegisterActivity.class));
    }
    @Override
    public void startANCSmartRegistry() {
         activity.startActivity(new Intent(activity, NativeKIANCSmartRegisterActivity.class));
    }
    @Override
    public void startPNCSmartRegistry() {
        activity.startActivity(new Intent(activity, NativeKIPNCSmartRegisterActivity.class));
    }
    @Override
    public void startChildSmartRegistry() {
        activity.startActivity(new Intent(activity, NativeKIAnakSmartRegisterActivity.class));
    }
    @Override
    public void startReports() {
        String id, pass;
        try{
            id = new JSONObject(anmController.get()).get("anmName").toString();
            pass = context.allSettings().fetchANMPassword();
        }catch(org.json.JSONException ex){
            id="noname";
            pass="null";
        }
        String uri = "http://"+id+":"+pass+"@"+activity.getApplicationContext().getString(R.string.dho_site).replace("http://","");
        activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
    }

}