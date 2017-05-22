package com.example.opensrp_anc.anc;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.ei.opensrp.core.template.DetailFragment;
import org.ei.opensrp.core.template.RegisterActivity;
import org.ei.opensrp.core.template.RegisterDataGridFragment;
import org.ei.opensrp.view.controller.FormController;

import java.util.ArrayList;
import java.util.List;

public class ANCSmartRegisterActivity extends RegisterActivity {

    private String id;
    @Override
    protected void onCreateActivity(Bundle savedInstanceState) {
        super.onCreateActivity(savedInstanceState);

        Log.v(getClass().getName(), "savedInstanceState bundle : "+savedInstanceState);
        Log.v(getClass().getName(), "intent bundle : "+getIntent().toString());
        id = getIntent().getStringExtra("program_client_id");
    }
    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        Log.i(getClass().getName(), "Resuming fragments");
    }
    private void filter(){
        RegisterDataGridFragment registerFragment = getBaseFragment();
        if(registerFragment != null && registerFragment.loaderHandler().fullyLoaded()){
            registerFragment.getSearchView().setText(id);
            registerFragment.onFilterManual(id);
        }
        else {
            new Handler(getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    filter();
                }
            }, 2000);
        }
    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Log.i(getClass().getName(), "Win focus changed and filtering for ID "+id);
        if(id != null && !id.isEmpty()){
            filter();
        }
    }
    @Override
    protected RegisterDataGridFragment makeBaseFragment() {
        return new ANCSmartRegisterFragment(new FormController(this));
    }

    @Override
    protected String[] buildFormNameList() {
        List<String> formNames = new ArrayList<String>();
       // formNames.add("Test_form1");
       // formNames.add("Test_form2");
        formNames.add("anc_visit_form");

        return formNames.toArray(new String[formNames.size()]);
    }

    @Override
    public String postFormSubmissionRecordFilterField() {

        return "existing_program_client_id";
    }

    @Override
    protected void onResumption() {

    }
    @Override
    public DetailFragment getDetailFragment() {
        return new ANCDetailFragment();
    }
}
