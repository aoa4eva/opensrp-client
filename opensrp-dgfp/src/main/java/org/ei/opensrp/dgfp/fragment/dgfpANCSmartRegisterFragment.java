package org.ei.opensrp.dgfp.fragment;

import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import org.ei.opensrp.Context;
import org.ei.opensrp.adapter.SmartRegisterPaginatedAdapter;
import org.ei.opensrp.commonregistry.CommonPersonObject;
import org.ei.opensrp.commonregistry.CommonPersonObjectClient;
import org.ei.opensrp.commonregistry.CommonPersonObjectController;
import org.ei.opensrp.commonregistry.CommonRepository;
import org.ei.opensrp.commonregistry.ControllerFilterMap;
import org.ei.opensrp.cursoradapter.CursorCommonObjectFilterOption;
import org.ei.opensrp.cursoradapter.CursorCommonObjectSort;
import org.ei.opensrp.cursoradapter.SecuredNativeSmartRegisterCursorAdapterFragment;
import org.ei.opensrp.cursoradapter.SmartRegisterPaginatedCursorAdapter;
import org.ei.opensrp.cursoradapter.SmartRegisterQueryBuilder;
import org.ei.opensrp.dgfp.LoginActivity;
import org.ei.opensrp.dgfp.R;
import org.ei.opensrp.dgfp.anc.mCareANCServiceModeOption;
import org.ei.opensrp.dgfp.anc.mCareANCSmartClientsProvider;
import org.ei.opensrp.dgfp.anc.mCareANCSmartRegisterActivity;
import org.ei.opensrp.dgfp.anc.mCareAncDetailActivity;
import org.ei.opensrp.dgfp.hh_member.HouseholdCensusDueDateSort;
import org.ei.opensrp.provider.SmartRegisterClientsProvider;
import org.ei.opensrp.util.StringUtil;
import org.ei.opensrp.view.activity.SecuredNativeSmartRegisterActivity;
import org.ei.opensrp.view.contract.ECClient;
import org.ei.opensrp.view.contract.SmartRegisterClient;
import org.ei.opensrp.view.contract.SmartRegisterClients;
import org.ei.opensrp.view.controller.VillageController;
import org.ei.opensrp.view.customControls.CustomFontTextView;
import org.ei.opensrp.view.dialog.AllClientsFilter;
import org.ei.opensrp.view.dialog.DialogOption;
import org.ei.opensrp.view.dialog.DialogOptionMapper;
import org.ei.opensrp.view.dialog.DialogOptionModel;
import org.ei.opensrp.view.dialog.EditOption;
import org.ei.opensrp.view.dialog.FilterOption;
import org.ei.opensrp.view.dialog.ServiceModeOption;
import org.ei.opensrp.view.dialog.SortOption;
import org.opensrp.api.domain.Location;
import org.opensrp.api.util.EntityUtils;
import org.opensrp.api.util.LocationTree;
import org.opensrp.api.util.TreeNode;

import java.util.ArrayList;
import java.util.Map;

import util.AsyncTask;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * Created by koros on 11/2/15.
 */
public class dgfpANCSmartRegisterFragment extends SecuredNativeSmartRegisterCursorAdapterFragment {

    private SmartRegisterClientsProvider clientProvider = null;
    private CommonPersonObjectController controller;
    private VillageController villageController;
    private DialogOptionMapper dialogOptionMapper;

    private final ClientActionHandler clientActionHandler = new ClientActionHandler();

    @Override
    protected SmartRegisterPaginatedAdapter adapter() {
        return new SmartRegisterPaginatedAdapter(clientsProvider());
    }

    @Override
    protected SecuredNativeSmartRegisterActivity.DefaultOptionsProvider getDefaultOptionsProvider() {
        return new SecuredNativeSmartRegisterActivity.DefaultOptionsProvider() {

            @Override
            public ServiceModeOption serviceMode() {
                return new mCareANCServiceModeOption(clientsProvider());
            }

            @Override
            public FilterOption villageFilter() {
                return new AllClientsFilter();
            }

            @Override
            public SortOption sortOption() {
//                return null;
                return new HouseholdCensusDueDateSort();

            }

            @Override
            public String nameInShortFormForTitle() {
                return getResources().getString(R.string.mcare_ANC_register_title_in_short);
            }
        };
    }

    @Override
    protected SecuredNativeSmartRegisterActivity.NavBarOptionsProvider getNavBarOptionsProvider() {
        return new SecuredNativeSmartRegisterActivity.NavBarOptionsProvider() {

            @Override
            public DialogOption[] filterOptions() {
                ArrayList<DialogOption> dialogOptionslist = new ArrayList<DialogOption>();
                dialogOptionslist.add(new CursorCommonObjectFilterOption(getString(R.string.filter_by_all_label),""));
//                dialogOptionslist.add(new CursorCommonObjectFilterOption(getString(R.string.filter_by_anc1),filterStringForANCRV1()));
//                dialogOptionslist.add(new CursorCommonObjectFilterOption(getString(R.string.filter_by_anc2),filterStringForANCRV2()));
//                dialogOptionslist.add(new CursorCommonObjectFilterOption(getString(R.string.filter_by_anc3),filterStringForANCRV3()));
//                dialogOptionslist.add(new CursorCommonObjectFilterOption(getString(R.string.filter_by_anc4),filterStringForANCRV4()));

                String locationjson = context.anmLocationController().get();
                LocationTree locationTree = EntityUtils.fromJson(locationjson, LocationTree.class);

                Map<String,TreeNode<String, Location>> locationMap =
                        locationTree.getLocationsHierarchy();
                addChildToList(dialogOptionslist,locationMap);
                DialogOption[] dialogOptions = new DialogOption[dialogOptionslist.size()];
                for (int i = 0;i < dialogOptionslist.size();i++){
                    dialogOptions[i] = dialogOptionslist.get(i);
                }

                return  dialogOptions;
            }

            @Override
            public DialogOption[] serviceModeOptions() {
                return new DialogOption[]{};
            }

            @Override
            public DialogOption[] sortingOptions() {
                return new DialogOption[]{
//                        new ElcoPSRFDueDateSort(),
//                        new CursorCommonObjectSort(getString(R.string.due_status),sortByAlertmethod()),
                        new CursorCommonObjectSort(Context.getInstance().applicationContext().getString(R.string.elco_alphabetical_sort),sortByFWWOMFNAME())
//                        new CursorCommonObjectSort(Context.getInstance().applicationContext().getString(R.string.hh_fwGobhhid_sort),sortByGOBHHID()),
//                        new CursorCommonObjectSort( Context.getInstance().applicationContext().getString(R.string.hh_fwJivhhid_sort),sortByJiVitAHHID()),
//                        new CursorCommonObjectSort( Context.getInstance().applicationContext().getString(R.string.sortbyLmp),sortByLmp())

//                        new CommonObjectSort(true,false,true,"age")
                };
            }

            @Override
            public String searchHint() {
                return getString(R.string.search_register);
            }
        };
    }

    @Override
    protected SmartRegisterClientsProvider clientsProvider() {

        return null;
    }


    @Override
    protected void onInitialization() {

//        context.formSubmissionRouter().getHandlerMap().put("psrf_form",new PSRFHandler());
    }

    @Override
    protected void startRegistration() {
//        ((ElcoSmartRegisterActivity)getActivity()).startRegistration();
    }

    @Override
    protected void onCreation() {
    }
    @Override
    protected void onResumption() {
        super.onResumption();
        getDefaultOptionsProvider();
        if(isPausedOrRefreshList()) {
            initializeQueries();
        }
        updateSearchView();
        try{
            LoginActivity.setLanguage();
        }catch (Exception e){

        }

    }

    @Override
    public void setupViews(View view) {
        super.setupViews(view);
        view.findViewById(R.id.btn_report_month).setVisibility(INVISIBLE);
        view.findViewById(R.id.service_mode_selection).setVisibility(INVISIBLE);

        ImageButton startregister = (ImageButton)view.findViewById(org.ei.opensrp.R.id.register_client);
        startregister.setVisibility(View.GONE);
        clientsView.setVisibility(View.VISIBLE);
        clientsProgressView.setVisibility(View.INVISIBLE);
//        list.setBackgroundColor(Color.RED);
        initializeQueries();
    }

    private DialogOption[] getEditOptions() {
        return ((mCareANCSmartRegisterActivity)getActivity()).getEditOptions();
    }
    private DialogOption[] getEditOptionsforanc(String ancvisittext,String ancvisitstatus) {
        return ((mCareANCSmartRegisterActivity)getActivity()).getEditOptionsforanc(ancvisittext,ancvisitstatus);
    }



    private class ClientActionHandler implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.profile_info_layout:
                    mCareAncDetailActivity.ancclient = (CommonPersonObjectClient)view.getTag();
                    Intent intent = new Intent(getActivity(),mCareAncDetailActivity.class);
                    startActivity(intent);
                    break;
                case R.id.anc_reminder_due_date:
                    CustomFontTextView ancreminderDueDate = (CustomFontTextView)view.findViewById(R.id.anc_reminder_due_date);
                    Log.v("do as you will", (String) view.getTag(R.id.textforAncRegister));
                    showFragmentDialog(new EditDialogOptionModelForANC((String)view.getTag(R.id.textforAncRegister),(String)view.getTag(R.id.AlertStatustextforAncRegister)), view.getTag(R.id.clientobject));
                    break;
                case R.id.nbnf_due_date:
                    showFragmentDialog(new EditDialogOptionModelfornbnf(), view.getTag(R.id.clientobject));
                    break;
            }
        }

        private void showProfileView(ECClient client) {
            navigationController.startEC(client.entityId());
        }
    }
    private class EditDialogOptionModelfornbnf implements DialogOptionModel {
        @Override
        public DialogOption[] getDialogOptions() {
            return getEditOptions();
        }

        @Override
        public void onDialogOptionSelection(DialogOption option, Object tag) {
            onEditSelection((EditOption) option, (SmartRegisterClient) tag);
        }
    }
    private class EditDialogOptionModelForANC implements DialogOptionModel {
        String ancvisittext ;;
        String Ancvisitstatus;
        public EditDialogOptionModelForANC(String text,String status) {
            ancvisittext = text;
            Ancvisitstatus = status;
        }

        @Override
        public DialogOption[] getDialogOptions() {
            return getEditOptionsforanc(ancvisittext,Ancvisitstatus);
        }

        @Override
        public void onDialogOptionSelection(DialogOption option, Object tag) {
            onEditSelection((EditOption) option, (SmartRegisterClient) tag);
        }
    }



    public void updateSearchView(){
        getSearchView().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(final CharSequence cs, int start, int before, int count) {
                (new AsyncTask() {
                    SmartRegisterClients filteredClients;

                    @Override
                    protected Object doInBackground(Object[] params) {
//                        currentSearchFilter =
//                        setCurrentSearchFilter(new HHSearchOption(cs.toString()));
//                        filteredClients = getClientsAdapter().getListItemProvider()
//                                .updateClients(getCurrentVillageFilter(), getCurrentServiceModeOption(),
//                                        getCurrentSearchFilter(), getCurrentSortOption());
//
                        if(cs.toString().equalsIgnoreCase("")){
                            filters = "";
                        }else {
                            //filters = "and FWWOMFNAME Like '%" + cs.toString() + "%' or GOBHHID Like '%" + cs.toString() + "%'  or JiVitAHHID Like '%" + cs.toString() + "%' ";
                            filters = cs.toString();
                        }
                        joinTable = "";
                        if(cs.toString().equalsIgnoreCase("")){
                            filters = "";
                        }else {
                            filters = "and (Mem_F_Name Like '%" + cs.toString() + "%' or Member_GOB_HHID Like '%" + cs.toString() + "%'  or details Like '%" + cs.toString() + "%') ";
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Object o) {
//                        clientsAdapter
//                                .refreshList(currentVillageFilter, currentServiceModeOption,
//                                        currentSearchFilter, currentSortOption);
//                        getClientsAdapter().refreshClients(filteredClients);
//                        getClientsAdapter().notifyDataSetChanged();
                        getSearchCancelView().setVisibility(isEmpty(cs) ? INVISIBLE : VISIBLE);
                        CountExecute();
                        filterandSortExecute();
                        super.onPostExecute(o);
                    }
                }).execute();


            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }
    public void addChildToList(ArrayList<DialogOption> dialogOptionslist,Map<String,TreeNode<String, Location>> locationMap){
        for(Map.Entry<String, TreeNode<String, Location>> entry : locationMap.entrySet()) {

            if(entry.getValue().getChildren() != null) {
                addChildToList(dialogOptionslist,entry.getValue().getChildren());

            }else{
                StringUtil.humanize(entry.getValue().getLabel());
                String name = StringUtil.humanize(entry.getValue().getLabel());
//                dialogOptionslist.add(new ElcoMauzaCommonObjectFilterOption(name,"location_name", name));

            }
        }
    }
    class ancControllerfiltermap extends ControllerFilterMap{

        @Override
        public boolean filtermapLogic(CommonPersonObject commonPersonObject) {
            boolean returnvalue = false;
            if(commonPersonObject.getDetails().get("FWWOMVALID") != null){
                if(commonPersonObject.getDetails().get("FWWOMVALID").equalsIgnoreCase("1")){
                    returnvalue = true;
                    if(commonPersonObject.getDetails().get("Is_PNC")!=null){
                        if(commonPersonObject.getDetails().get("Is_PNC").equalsIgnoreCase("1")){
                            returnvalue = false;
                        }

                    }
                }
            }
            Log.v("the filter",""+returnvalue);
            return returnvalue;
        }
    }
    public String ancMainSelectWithJoins(){
        return "Select id as _id,relationalid,details,Mem_F_Name,EDD,Calc_Dob_Confirm,Child_calc_age,calc_age_confirm,Child_mother_name,Member_GOB_HHID,Marital_status,Pregnancy_Status,missedCount \n" +
                "from members " ;
    }
    public String ancMainCountWithJoins(){
        return "Select Count(*) \n" +
                "from members ";
    }
    public void initializeQueries(){
        mCareANCSmartClientsProvider hhscp = new mCareANCSmartClientsProvider(getActivity(),clientActionHandler,context.alertService());
        clientAdapter = new SmartRegisterPaginatedCursorAdapter(getActivity(), null, hhscp, new CommonRepository("members",new String []{"Mem_F_Name","EDD","Calc_Dob_Confirm","Child_calc_age","calc_age_confirm","Child_mother_name","Member_GOB_HHID","Marital_status","Pregnancy_Status","missedCount"}));
        clientsView.setAdapter(clientAdapter);

        setTablename("members");
        SmartRegisterQueryBuilder countqueryBUilder = new SmartRegisterQueryBuilder(ancMainCountWithJoins());
        countSelect = countqueryBUilder.mainCondition("(members.Mem_F_Name not null ) AND members.details  LIKE '%\"Preg_Status\":\"1\"%'");
        mainCondition = "(members.Mem_F_Name not null ) AND members.details  LIKE '%\"PW\":\"1\"%' and Not (details like '%\"Visit_Status\":\"10\"%' or details like '%\"Visit_Status\":\"11\"%' or details like '%\"Visit_Status\":\"8\"%')  ";
        super.CountExecute();

        SmartRegisterQueryBuilder queryBUilder = new SmartRegisterQueryBuilder(ancMainSelectWithJoins());
        mainSelect = queryBUilder.mainCondition("(members.Mem_F_Name not null ) AND members.details  LIKE '%\"Preg_Status\":\"1\"%' and Not (details like '%\"Visit_Status\":\"10\"%' or details like '%\"Visit_Status\":\"11\"%' or details like '%\"Visit_Status\":\"8\"%')  ");
        Sortqueries = sortByFWWOMFNAME();

        currentlimit = 20;
        currentoffset = 0;

        super.filterandSortInInitializeQueries();

        updateSearchView();
        refresh();

    }
    private String sortBySortValue(){
        return " FWSORTVALUE ASC";
    }
    private String sortByFWWOMFNAME(){
        return " Mem_F_Name ASC";
    }
    private String sortByJiVitAHHID(){
        return " JiVitAHHID ASC";
    }
    private String sortByLmp(){
        return " FWPSRLMP ASC";
    }
    private String filterStringForANCRV1(){
        return "and alerts.visitCode LIKE '%ancrv_1%'";
    }
    private String filterStringForANCRV2(){
        return "and alerts.visitCode LIKE '%ancrv_2%'";
    }
    private String filterStringForANCRV3(){
        return "and alerts.visitCode LIKE '%ancrv_3%'";
    }
    private String filterStringForANCRV4(){
        return "and alerts.visitCode LIKE '%ancrv_4%'";
    }
    private String sortByGOBHHID(){
        return " GOBHHID ASC";
    }
    private String sortByAlertmethod() {
        return " CASE WHEN alerts.status = 'urgent' and alerts2.status = 'urgent' THEN 1 "
                +
                "WHEN alerts.status = 'upcoming' and alerts2.status = 'urgent' THEN  2\n" +
                "WHEN alerts.status = 'normal' and alerts2.status = 'urgent' THEN 3\n" +
                "WHEN alerts.status = 'expired' and alerts2.status = 'urgent' THEN 4\n" +
                "WHEN alerts.status is null and alerts2.status = 'urgent' THEN 5\n" +

                "WHEN alerts.status = 'urgent' and alerts2.status = 'upcoming' THEN 6\n" +
                "WHEN alerts.status = 'upcoming' and alerts2.status = 'upcoming' THEN 7\n" +
                "WHEN alerts.status = 'normal' and alerts2.status = 'upcoming' THEN 8\n" +
                "WHEN alerts.status = 'expired' and alerts2.status = 'upcoming' THEN 9\n" +
                "WHEN alerts.status is null and alerts2.status = 'upcoming' THEN 10\n" +

                "WHEN alerts.status = 'urgent' and alerts2.status = 'normal' THEN 11\n" +
                "WHEN alerts.status = 'upcoming' and alerts2.status = 'normal' THEN 12\n" +
                "WHEN alerts.status = 'normal' and alerts2.status = 'normal' THEN 13\n" +
                "WHEN alerts.status = 'expired' and alerts2.status = 'normal' THEN 14\n" +
                "WHEN alerts.status is null and alerts2.status = 'normal' THEN 15\n" +

                "WHEN alerts.status = 'urgent' and alerts2.status = 'expired' THEN 16\n" +
                "WHEN alerts.status = 'upcoming' and alerts2.status = 'expired' THEN 17\n" +
                "WHEN alerts.status = 'normal' and alerts2.status = 'expired' THEN 18\n" +
                "WHEN alerts.status = 'expired' and alerts2.status = 'expired' THEN 19\n" +
                "WHEN alerts.status is null and alerts2.status = 'expired' THEN 20\n" +

                "WHEN alerts2.status is null THEN 9999\n" +
                "WHEN alerts.status = \"\" THEN 99999\n" +
//                "WHEN alerts2.status is null THEN '18'\n" +
                "Else alerts.status END ASC";
    }
}
