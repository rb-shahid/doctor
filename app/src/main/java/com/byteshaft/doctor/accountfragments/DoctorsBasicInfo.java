package com.byteshaft.doctor.accountfragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.byteshaft.doctor.MainActivity;
import com.byteshaft.doctor.R;
import com.byteshaft.doctor.adapters.AffiliateClinicAdapter;
import com.byteshaft.doctor.adapters.CitiesAdapter;
import com.byteshaft.doctor.adapters.StatesAdapter;
import com.byteshaft.doctor.adapters.SubscriptionTypeAdapter;
import com.byteshaft.doctor.doctors.Dashboard;
import com.byteshaft.doctor.doctors.DoctorsList;
import com.byteshaft.doctor.gettersetter.AffiliateClinic;
import com.byteshaft.doctor.gettersetter.Cities;
import com.byteshaft.doctor.gettersetter.Specialities;
import com.byteshaft.doctor.gettersetter.States;
import com.byteshaft.doctor.gettersetter.SubscriptionType;
import com.byteshaft.doctor.utils.AppGlobals;
import com.byteshaft.doctor.utils.Helpers;
import com.byteshaft.requests.FormData;
import com.byteshaft.requests.HttpRequest;
import com.github.lzyzsd.circleprogress.DonutProgress;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.util.ArrayList;

import static android.R.attr.id;

public class DoctorsBasicInfo extends Fragment implements AdapterView.OnItemSelectedListener,
        CompoundButton.OnCheckedChangeListener, View.OnClickListener, HttpRequest.OnReadyStateChangeListener, HttpRequest.OnFileUploadProgressListener, HttpRequest.OnErrorListener {

    private View mBaseView;
    private Button mSaveButton;

    private Spinner mStateSpinner;
    private Spinner mCitySpinner;
    private Spinner mSpecialitySpinner;
    private Spinner mAffiliatedClinicsSpinner;
    private Spinner mSubscriptionSpinner;

    private EditText mPhoneOneEditText;
    private EditText mPhoneTwoEditText;
    private EditText mConsultationTimeEditText;
    private EditText mCollegeIdEditText;

    private CheckBox mNotificationCheckBox;
    private CheckBox mNewsCheckBox;
    private CheckBox mTermsConditionCheckBox;
    private String mPhoneOneEditTextString;
    private String mPhoneTwoEditTextString;
    private String mConsultationTimeEditTextString;
    private String mCollegeIdEditTextString;
    private String mStatesSpinnerValueString;
    private String mCitiesSpinnerValueString;
    private String mSpecialitySpinnerValueString;
    private String mAffiliatedClinicsSpinnerValueString;
    private String mSubscriptionSpinnerValueString;
    private String mNotificationCheckBoxString = "true";
    private String mNewsCheckBoxString = "true";
    private String mTermsConditionCheckBoxString;

    private HttpRequest mRequest;
    private DonutProgress donutProgress;
    private ProgressBar progressBar;
    private AlertDialog.Builder alertDialogBuilder;
    private AlertDialog alertDialog;
    // Date lists
    private ArrayList<States> statesList;
    private StatesAdapter statesAdapter;
    private ArrayList<Cities> citiesList;
    private CitiesAdapter citiesAdapter;

    private ArrayList<Specialities> specialitiesList;
    private SpecialitiesAdapter specialitiesAdapter;

    private ArrayList<AffiliateClinic> affiliateClinicsList;
    private AffiliateClinicAdapter affiliateClinicAdapter;

    private ArrayList<SubscriptionType> subscriptionTypesList;
    private SubscriptionTypeAdapter subscriptionTypeAdapter;

    private int cityPosition;
    private int statePosition;
    private int subscriptionPosition;
    private int affiliateClinicPosition;
    private int specialistPosition;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBaseView = inflater.inflate(R.layout.fragment_doctor_basic_info, container, false);
        if (AppGlobals.isLogin() && AppGlobals.isInfoAvailable()) {
            ((AppCompatActivity) getActivity()).getSupportActionBar()
                    .setTitle(getResources().getString(R.string.update_profile));
        } else {
            ((AppCompatActivity) getActivity()).getSupportActionBar()
                    .setTitle(getResources().getString(R.string.sign_up));
        }
        setHasOptionsMenu(true);
        getStates();
        getSpecialities();
        getAffiliateClinic();
        getSubscriptionType();
        /// data list work
        statesList = new ArrayList<>();
        citiesList = new ArrayList<>();
        specialitiesList = new ArrayList<>();
        affiliateClinicsList = new ArrayList<>();
        subscriptionTypesList = new ArrayList<>();

        mSaveButton = (Button) mBaseView.findViewById(R.id.save_button);
        mStateSpinner = (Spinner) mBaseView.findViewById(R.id.states_spinner);
        mCitySpinner = (Spinner) mBaseView.findViewById(R.id.cities_spinner);
        mSpecialitySpinner = (Spinner) mBaseView.findViewById(R.id.speciality_spinner);
        mAffiliatedClinicsSpinner = (Spinner) mBaseView.findViewById(R.id.clinics_spinner);
        mSubscriptionSpinner = (Spinner) mBaseView.findViewById(R.id.subscriptions_spinner);

        mPhoneOneEditText = (EditText) mBaseView.findViewById(R.id.phone_one_edit_text);
        mPhoneTwoEditText = (EditText) mBaseView.findViewById(R.id.phone_two_edit_text);
        mConsultationTimeEditText = (EditText) mBaseView.findViewById(R.id.consultation_time_edit_text);
        mCollegeIdEditText = (EditText) mBaseView.findViewById(R.id.college_id_edit_text);

        mNotificationCheckBox = (CheckBox) mBaseView.findViewById(R.id.notifications_check_box);
        mNewsCheckBox = (CheckBox) mBaseView.findViewById(R.id.news_check_box);
        mTermsConditionCheckBox = (CheckBox) mBaseView.findViewById(R.id.terms_check_box);

        mNotificationCheckBox.setChecked(AppGlobals.isShowNotification());
        mNewsCheckBox.setChecked(AppGlobals.isShowNews());

        mSaveButton.setTypeface(AppGlobals.typefaceNormal);
        mPhoneOneEditText.setTypeface(AppGlobals.typefaceNormal);
        mPhoneTwoEditText.setTypeface(AppGlobals.typefaceNormal);
        mConsultationTimeEditText.setTypeface(AppGlobals.typefaceNormal);
        mCollegeIdEditText.setTypeface(AppGlobals.typefaceNormal);

        mPhoneOneEditText.setText(AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_PHONE_NUMBER_PRIMARY));
        mPhoneTwoEditText.setText(AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_PHONE_NUMBER_SECONDARY));
        mConsultationTimeEditText.setText(AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_CONSULTATION_TIME));
        mCollegeIdEditText.setText(AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_COLLEGE_ID));

        mStateSpinner.setOnItemSelectedListener(this);
        mCitySpinner.setOnItemSelectedListener(this);
        mSpecialitySpinner.setOnItemSelectedListener(this);
        mAffiliatedClinicsSpinner.setOnItemSelectedListener(this);
        mSubscriptionSpinner.setOnItemSelectedListener(this);

        mNotificationCheckBox.setOnCheckedChangeListener(this);
        mNewsCheckBox.setOnCheckedChangeListener(this);
        mTermsConditionCheckBox.setOnCheckedChangeListener(this);
        if (AppGlobals.isLogin() && AppGlobals.isInfoAvailable()) {
            mTermsConditionCheckBox.setVisibility(View.GONE);
        }
        mSaveButton.setOnClickListener(this);
        return mBaseView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getFragmentManager().popBackStack();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        switch (adapterView.getId()) {
            case R.id.states_spinner:
                States states = statesList.get(i);
                getCities(states.getId());
                mStatesSpinnerValueString = String.valueOf(states.getId());
                System.out.println(states.getId());
                AppGlobals.saveDoctorProfileIds(AppGlobals.KEY_STATE_SELECTED,
                        states.getId());
                break;
            case R.id.cities_spinner:
                if (citiesList.size() > 0) {
                    Cities city = citiesList.get(i);
                    mCitiesSpinnerValueString = String.valueOf(city.getCityId());
                    System.out.println(city.getCityId());
                    AppGlobals.saveDoctorProfileIds(AppGlobals.KEY_CITY_SELECTED,
                            city.getCityId());
                }
                break;
            case R.id.speciality_spinner:
                Specialities specialities = specialitiesList.get(i);
                mSpecialitySpinnerValueString = String.valueOf(specialities.getSpecialitiesId());
                System.out.println(specialities.getSpecialitiesId());
                AppGlobals.saveDoctorProfileIds(AppGlobals.KEY_SPECIALIST_SELECTED,
                        specialities.getSpecialitiesId());
                break;
            case R.id.clinics_spinner:
                AffiliateClinic affiliateClinic = affiliateClinicsList.get(i);
                mAffiliatedClinicsSpinnerValueString = String.valueOf(affiliateClinic.getId());
                System.out.println(affiliateClinic.getId());
                AppGlobals.saveDoctorProfileIds(AppGlobals.KEY_CLINIC_SELECTED,
                        affiliateClinic.getId());
                break;
            case R.id.subscriptions_spinner:
                SubscriptionType subscriptionType = subscriptionTypesList.get(i);
                mSubscriptionSpinnerValueString = String.valueOf(subscriptionType.getId());
                System.out.println(subscriptionType.getId() + "  " + subscriptionType.getPrice());
                AppGlobals.saveDoctorProfileIds(AppGlobals.KEY_SUBSCRIPTION_SELECTED,
                        subscriptionType.getId());
                break;
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()) {
            case R.id.notifications_check_box:
                if (mNotificationCheckBox.isChecked()) {
                    mNotificationCheckBoxString = "true";
                    System.out.println(mNotificationCheckBoxString);
                }
                break;
            case R.id.news_check_box:
                if (mNewsCheckBox.isChecked()) {
                    mNewsCheckBoxString = "true";
                    System.out.println(mNewsCheckBoxString);
                }
                break;
            case R.id.terms_check_box:
                if (mTermsConditionCheckBox.isChecked()) {
                    mSaveButton.setEnabled(true);
                    mSaveButton.setBackgroundColor(getResources().getColor(R.color.buttonColor));
                    mTermsConditionCheckBoxString = mTermsConditionCheckBox.getText().toString();
                    System.out.println(mTermsConditionCheckBoxString);
                } else {
                    mSaveButton.setEnabled(false);
                    mSaveButton.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                }
                break;
        }
    }

    @Override
    public void onClick(View view) {
        mPhoneTwoEditTextString = mPhoneTwoEditText.getText().toString();
        if (validateEditText()) {
            sendingDataToServer();
        }

    }

    private boolean validateEditText() {
        boolean valid = true;
        mPhoneOneEditTextString = mPhoneOneEditText.getText().toString();
        mCollegeIdEditTextString = mCollegeIdEditText.getText().toString();
        mConsultationTimeEditTextString = mConsultationTimeEditText.getText().toString();

        if (mPhoneOneEditTextString.trim().isEmpty()) {
            mPhoneOneEditText.setError("please enter your phone number");
            valid = false;
        } else {
            mPhoneOneEditText.setError(null);
        }
        if (mCollegeIdEditTextString.trim().isEmpty()) {
            mCollegeIdEditText.setError("please provide your collegeID");
            valid = false;
        } else {
            mCollegeIdEditText.setError(null);
        }
        if (mConsultationTimeEditTextString.trim().isEmpty()) {
            mConsultationTimeEditText.setError("please enter your consultation time");
            valid = false;
        } else {
            mConsultationTimeEditText.setError(null);
        }

        return valid;
    }

    private void sendingDataToServer() {
        FormData data = new FormData();
        data.append(FormData.TYPE_CONTENT_TEXT, "identity_document", AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_DOC_ID));
        data.append(FormData.TYPE_CONTENT_TEXT, "first_name", AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_FIRST_NAME));
        data.append(FormData.TYPE_CONTENT_TEXT, "last_name", AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_LAST_NAME));
        data.append(FormData.TYPE_CONTENT_TEXT, "dob", AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_DATE_OF_BIRTH));
        data.append(FormData.TYPE_CONTENT_TEXT, "gender", AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_GENDER));
        data.append(FormData.TYPE_CONTENT_TEXT, "location", AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_LOCATION));
        data.append(FormData.TYPE_CONTENT_TEXT, "address", AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_ADDRESS));
        Log.i("TAG", "key image url " + AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_IMAGE_URL));
        if (!AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_IMAGE_URL).trim().isEmpty()) {
            data.append(FormData.TYPE_CONTENT_FILE, "photo", AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_IMAGE_URL));
            alertDialogBuilder = new AlertDialog.Builder(getActivity());
            alertDialogBuilder.setTitle(getResources().getString(R.string.updating));
            alertDialogBuilder.setCancelable(false);
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.progress_alert_dialog, null);
            alertDialogBuilder.setView(dialogView);
            donutProgress = (DonutProgress) dialogView.findViewById(R.id.upload_progress);

            alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        } else {
            Helpers.showProgressDialog(getActivity(), "Updating your Profile...");
        }
        data.append(FormData.TYPE_CONTENT_TEXT, "state", mStatesSpinnerValueString);
        data.append(FormData.TYPE_CONTENT_TEXT, "city", mCitiesSpinnerValueString);
        data.append(FormData.TYPE_CONTENT_TEXT, "speciality", mSpecialitySpinnerValueString);
        data.append(FormData.TYPE_CONTENT_TEXT, "affiliate_clinic", mAffiliatedClinicsSpinnerValueString);
        data.append(FormData.TYPE_CONTENT_TEXT, "subscription_plan", mSubscriptionSpinnerValueString);
        data.append(FormData.TYPE_CONTENT_TEXT, "phone_number_primary", mPhoneOneEditTextString);
        data.append(FormData.TYPE_CONTENT_TEXT, "phone_number_secondary", mPhoneTwoEditTextString);
        data.append(FormData.TYPE_CONTENT_TEXT, "consultation_time", mConsultationTimeEditTextString);
        data.append(FormData.TYPE_CONTENT_TEXT, "college_id", mCollegeIdEditTextString);
        data.append(FormData.TYPE_CONTENT_TEXT, "show_notification", mNotificationCheckBoxString);
        data.append(FormData.TYPE_CONTENT_TEXT, "show_news", mNewsCheckBoxString);
        mRequest = new HttpRequest(getActivity().getApplicationContext());
        mRequest.setOnReadyStateChangeListener(this);
        mRequest.setOnErrorListener(this);
        mRequest.setOnFileUploadProgressListener(this);
        String method = "POST";
        if (AppGlobals.isLogin() && AppGlobals.isInfoAvailable()) {
            method = "PUT";
        }
        mRequest.open(method, String.format("%sprofile", AppGlobals.BASE_URL));
        mRequest.setRequestHeader("Authorization", "Token " +
                AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_TOKEN));
        mRequest.send(data);
    }

    private void getAffiliateClinic() {

        HttpRequest affiliateClinicRequest = new HttpRequest(getActivity().getApplicationContext());
        affiliateClinicRequest.setOnReadyStateChangeListener(new HttpRequest.OnReadyStateChangeListener() {
            @Override
            public void onReadyStateChange(HttpRequest request, int readyState) {
                switch (readyState) {
                    case HttpRequest.STATE_DONE:
                        switch (request.getStatus()) {
                            case HttpURLConnection.HTTP_OK:
                                try {
                                    JSONObject spObject = new JSONObject(request.getResponseText());
                                    JSONArray spArray = spObject.getJSONArray("results");
                                    for (int i = 0; i < spArray.length(); i++) {
                                        JSONObject jsonObject = spArray.getJSONObject(i);
                                        AffiliateClinic affiliateClinic = new AffiliateClinic();
                                        affiliateClinic.setId(jsonObject.getInt("id"));
                                        if (AppGlobals.getDoctorProfileIds(AppGlobals.KEY_CLINIC_SELECTED)
                                                == jsonObject.getInt("id")) {
                                            affiliateClinicPosition = i;
                                        }
                                        affiliateClinic.setName(jsonObject.getString("name"));
                                        affiliateClinicsList.add(affiliateClinic);
                                    }
                                    affiliateClinicAdapter = new AffiliateClinicAdapter(
                                            getActivity(), affiliateClinicsList);
                                    mAffiliatedClinicsSpinner.setAdapter(affiliateClinicAdapter);
                                    mAffiliatedClinicsSpinner.setSelection(affiliateClinicPosition);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                        }
                }
            }
        });

        affiliateClinicRequest.open("GET", String.format("%sclinics/", AppGlobals.BASE_URL));
        affiliateClinicRequest.send();
    }

    private void getSpecialities() {

        HttpRequest specialitiesRequest = new HttpRequest(getActivity().getApplicationContext());
        specialitiesRequest.setOnReadyStateChangeListener(new HttpRequest.OnReadyStateChangeListener() {
            @Override
            public void onReadyStateChange(HttpRequest request, int readyState) {
                switch (readyState) {
                    case HttpRequest.STATE_DONE:
                        switch (request.getStatus()) {
                            case HttpURLConnection.HTTP_OK:
                                try {
                                    JSONObject spObject = new JSONObject(request.getResponseText());
                                    JSONArray spArray = spObject.getJSONArray("results");
                                    for (int i = 0; i < spArray.length(); i++) {
                                        JSONObject jsonObject = spArray.getJSONObject(i);
                                        Specialities specialities = new Specialities();
                                        specialities.setSpecialitiesId(jsonObject.getInt("id"));
                                        if (jsonObject.getInt("id") ==
                                                AppGlobals.getDoctorProfileIds(AppGlobals.KEY_SPECIALIST_SELECTED)) {
                                            specialistPosition = i;
                                        }
                                        specialities.setSpeciality(jsonObject.getString("name"));
                                        specialitiesList.add(specialities);
                                    }
                                    specialitiesAdapter = new SpecialitiesAdapter(specialitiesList);
                                    mSpecialitySpinner.setAdapter(specialitiesAdapter);
                                    mSpecialitySpinner.setSelection(specialistPosition);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                        }
                }
            }
        });

        specialitiesRequest.open("GET", String.format("%sspecialities", AppGlobals.BASE_URL));
        specialitiesRequest.send();
    }

    private void getStates() {
        HttpRequest getStateRequest = new HttpRequest(getActivity().getApplicationContext());
        getStateRequest.setOnReadyStateChangeListener(new HttpRequest.OnReadyStateChangeListener() {
            @Override
            public void onReadyStateChange(HttpRequest request, int readyState) {
                switch (readyState) {
                    case HttpRequest.STATE_DONE:
                        switch (request.getStatus()) {
                            case HttpURLConnection.HTTP_OK:
                                try {
                                    JSONObject object = new JSONObject(request.getResponseText());
                                    JSONArray jsonArray = object.getJSONArray("results");
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        System.out.println("Test " + jsonArray.getJSONObject(i));
                                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                                        States states = new States();
                                        states.setCode(jsonObject.getString("code"));
                                        states.setId(jsonObject.getInt("id"));
                                        if (jsonObject.getInt("id") ==
                                                AppGlobals.getDoctorProfileIds(
                                                        AppGlobals.KEY_STATE_SELECTED)) {
                                            statePosition = i;
                                        }
                                        states.setName(jsonObject.getString("name"));
                                        statesList.add(states);
                                    }
                                    statesAdapter = new StatesAdapter(getActivity(), statesList);
                                    mStateSpinner.setAdapter(statesAdapter);
                                    mStateSpinner.setSelection(statePosition);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                        }
                }
            }
        });
        getStateRequest.open("GET", String.format("%sstates", AppGlobals.BASE_URL));
        getStateRequest.send();
    }

    private void getCities(int id) {
        HttpRequest getCitiesRequest = new HttpRequest(getActivity().getApplicationContext());
        getCitiesRequest.setOnReadyStateChangeListener(
                new HttpRequest.OnReadyStateChangeListener() {
                    @Override
                    public void onReadyStateChange(HttpRequest request, int readyState) {
                        switch (readyState) {
                            case HttpRequest.STATE_DONE:
                                switch (request.getStatus()) {
                                    case HttpURLConnection.HTTP_OK:
                                        System.out.println(request.getResponseText());
                                        try {
                                            JSONObject object = new JSONObject(request.getResponseText());
                                            JSONArray jsonArray = object.getJSONArray("results");
                                            citiesList = new ArrayList<>();
                                            for (int i = 0; i < jsonArray.length(); i++) {
                                                System.out.println("Test " + jsonArray.getJSONObject(i));
                                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                                Cities cities = new Cities();
                                                cities.setCityId(jsonObject.getInt("id"));
                                                if (AppGlobals.getDoctorProfileIds(AppGlobals.KEY_CITY_SELECTED) ==
                                                        jsonObject.getInt("id")) {
                                                    cityPosition = i;
                                                }
                                                cities.setCityName(jsonObject.getString("name"));
                                                cities.setStateId(jsonObject.getInt("id"));
//                                                cities.setStateName(jsonObject.getString("state_name"));
                                                citiesList.add(cities);
                                            }
                                            citiesAdapter = new CitiesAdapter(getActivity(), citiesList);
                                            mCitySpinner.setAdapter(citiesAdapter);
                                            mCitySpinner.setSelection(cityPosition);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                }
                        }
                    }
                });
        getCitiesRequest.open("GET", String.format("%sstates/%s/cities", AppGlobals.BASE_URL, id));
        getCitiesRequest.send();
    }

    private void getSubscriptionType() {
        HttpRequest getsubTypeRequest = new HttpRequest(getActivity().getApplicationContext());
        getsubTypeRequest.setOnReadyStateChangeListener(new HttpRequest.OnReadyStateChangeListener() {
            @Override
            public void onReadyStateChange(HttpRequest request, int readyState) {
                switch (readyState) {
                    case HttpRequest.STATE_DONE:
                        switch (request.getStatus()) {
                            case HttpURLConnection.HTTP_OK:
                                System.out.println(request.getResponseText());
                                try {
                                    JSONObject object = new JSONObject(request.getResponseText());
                                    JSONArray jsonArray = object.getJSONArray("results");
                                    citiesList = new ArrayList<>();
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        System.out.println("Test " + jsonArray.getJSONObject(i));
                                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                                        SubscriptionType subscriptionType = new SubscriptionType();
                                        subscriptionType.setPlanType(jsonObject.getString("plan_type"));
                                        subscriptionType.setDescription(jsonObject.getString("description"));
                                        subscriptionType.setPrice(BigDecimal.valueOf(jsonObject.getDouble("price")).floatValue());
                                        subscriptionType.setId(jsonObject.getInt("id"));
                                        if (AppGlobals.getDoctorProfileIds(AppGlobals.KEY_SUBSCRIPTION_SELECTED)
                                                == jsonObject.getInt("id")) {
                                            subscriptionPosition = i;
                                        }
                                        subscriptionTypesList.add(subscriptionType);
                                    }
                                    subscriptionTypeAdapter = new SubscriptionTypeAdapter(
                                            getActivity(), subscriptionTypesList);
                                    mSubscriptionSpinner.setAdapter(subscriptionTypeAdapter);
                                    mSubscriptionSpinner.setSelection(subscriptionPosition);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                        }
                }
            }
        });
        getsubTypeRequest.open("GET", String.format("%ssubscriptions/", AppGlobals.BASE_URL, id));
        getsubTypeRequest.send();
    }

    @Override
    public void onReadyStateChange(HttpRequest request, int readyState) {
        switch (readyState) {
            case HttpRequest.STATE_DONE:
                if (alertDialog != null) {
                    alertDialog.dismiss();
                } else {
                    Helpers.dismissProgressDialog();
                }
                switch (request.getStatus()) {
                    case HttpRequest.ERROR_NETWORK_UNREACHABLE:
                        AppGlobals.alertDialog(getActivity(), "Profile update Failed!", "please check your internet connection");
                        break;
                    case HttpURLConnection.HTTP_NOT_FOUND:
                        AppGlobals.alertDialog(getActivity(), "Profile update Failed!", "provide a valid EmailAddress");
                        break;
                    case HttpURLConnection.HTTP_UNAUTHORIZED:
                        if (AppGlobals.isLogin() && AppGlobals.isInfoAvailable()) {
                            AppGlobals.alertDialog(getActivity(), "Inactive Account", "Your account is inactive, " +
                                    "please wait gor admin's approval. You will receive a activation Email");
                        } else
                            AppGlobals.alertDialog(getActivity(), "Profile update Failed", "Please enter correct password");
                        break;

                    case HttpURLConnection.HTTP_BAD_REQUEST:
                        Log.i("TAG", " " + request.getResponseText());
                        break;
                    case HttpURLConnection.HTTP_FORBIDDEN:
                        break;
                    case HttpURLConnection.HTTP_CREATED:
                        Log.i("TAG", "res" + request.getResponseText());
                        parseServerResponse(request);
                        AppGlobals.gotInfo(true);
                        AccountManagerActivity.getInstance().finish();
                        startActivity(new Intent(getActivity(), MainActivity.class));
                        break;
                    case HttpURLConnection.HTTP_OK:
                        Log.i("TAG", request.getResponseText());
                        Helpers.showSnackBar(getView(), R.string.profile_updated);
                        parseServerResponse(request);
                        new android.os.Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (AppGlobals.isDoctor()) {
                                    MainActivity.getInstance().loadFragment(new Dashboard());
                                } else {
                                    MainActivity.getInstance().loadFragment(new DoctorsList());
                                }
                            }
                        }, 800);
                        break;

                }
        }

    }

    private void parseServerResponse(HttpRequest request) {
        try {
            JSONObject jsonObject = new JSONObject(request.getResponseText());

            String userId = jsonObject.getString(AppGlobals.KEY_PROFILE_ID);
            String firstName = jsonObject.getString(AppGlobals.KEY_FIRST_NAME);
            String lastName = jsonObject.getString(AppGlobals.KEY_LAST_NAME);

            String gender = jsonObject.getString(AppGlobals.KEY_GENDER);
            String dateOfBirth = jsonObject.getString(AppGlobals.KEY_DATE_OF_BIRTH);
            String phoneNumberPrimary = jsonObject.getString(AppGlobals.KEY_PHONE_NUMBER_PRIMARY);
            String phoneNumberSecondary = jsonObject.getString(AppGlobals.KEY_PHONE_NUMBER_SECONDARY);

            JSONObject affiliateClinicJsonObject = jsonObject.getJSONObject(AppGlobals.KEY_AFFILIATE_CLINIC);
            String affiliateClinic = affiliateClinicJsonObject.getString("name");
            JSONObject subscriptionTypeJsonObject = jsonObject.getJSONObject(AppGlobals.KEY_SUBSCRIPTION_TYPE);
            String subscriptionType = subscriptionTypeJsonObject.getString("plan_type");
            JSONObject specialityJsonObject = jsonObject.getJSONObject("speciality");
            String speciality = specialityJsonObject.getString("name");
            String address = jsonObject.getString(AppGlobals.KEY_ADDRESS);
            String location = jsonObject.getString(AppGlobals.KEY_LOCATION);
            boolean chatStatus = jsonObject.getBoolean(AppGlobals.KEY_CHAT_STATUS);

            String state = jsonObject.getString(AppGlobals.KEY_STATE);
            String city = jsonObject.getString(AppGlobals.KEY_CITY);
            String docId = jsonObject.getString(AppGlobals.KEY_DOC_ID);
            String collegeId = jsonObject.getString(AppGlobals.KEY_COLLEGE_ID);
            boolean showNews = jsonObject.getBoolean(AppGlobals.KEY_SHOW_NEWS);

            boolean showNotification = jsonObject.getBoolean(AppGlobals.KEY_SHOW_NOTIFICATION);
            String consultationTime = jsonObject.getString(AppGlobals.KEY_CONSULTATION_TIME);
            String reviewStars = jsonObject.getString(AppGlobals.KEY_REVIEW_STARS);
            String imageUrl = jsonObject.getString(AppGlobals.KEY_IMAGE_URL);


            //saving values
            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_USER_ID, userId);
            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_FIRST_NAME, firstName);
            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_LAST_NAME, lastName);

            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_GENDER, gender);
            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_DATE_OF_BIRTH, dateOfBirth);
            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_PHONE_NUMBER_PRIMARY, phoneNumberPrimary);
            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_PHONE_NUMBER_SECONDARY, phoneNumberSecondary);

            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_AFFILIATE_CLINIC, affiliateClinic);
            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_SUBSCRIPTION_TYPE, subscriptionType);
            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_ADDRESS, address);
            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_LOCATION, location);
            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_DOC_SPECIALITY, speciality);

//                            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_CHAT_STATUS, chatStatus);
            AppGlobals.saveChatStatus(chatStatus);
            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_STATE, state);
            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_CITY, city);
            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_DOC_ID, docId);
//                            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_SHOW_NEWS, showNews);
            AppGlobals.saveNewsState(showNews);
            AppGlobals.saveNotificationState(showNotification);
//                            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_SHOW_NOTIFICATION, showNotification);
            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_CONSULTATION_TIME, consultationTime);
            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_REVIEW_STARS, reviewStars);
            AppGlobals.saveDataToSharedPreferences(AppGlobals.KEY_COLLEGE_ID, collegeId);
            AppGlobals.saveDataToSharedPreferences(AppGlobals.SERVER_PHOTO_URL, imageUrl);
            Log.i("Emergency Contact", " " + AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_EMERGENCY_CONTACT));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFileUploadProgress(HttpRequest request, File file, long loaded, long total) {
        double progress = (loaded / (double) total) * 100;
        Log.i("current progress", "" + (int) progress);
        donutProgress.setProgress((int) progress);
        if ((int) progress == 100) {
            Log.i("PROGRESS", "condition matched");
            if (alertDialog != null) {
                donutProgress.setProgress(100);
                alertDialog.dismiss();
            }
            alertDialogBuilder = new AlertDialog.Builder(getActivity());
            alertDialogBuilder.setTitle(getResources().getString(R.string.finishing_up));
            alertDialogBuilder.setCancelable(false);
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.finishingup_dialog, null);
            alertDialogBuilder.setView(dialogView);
            progressBar = (ProgressBar) dialogView.findViewById(R.id.progress_bar);
            alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }

    }

    @Override
    public void onError(HttpRequest request, int readyState, short error, Exception exception) {
        if (alertDialog != null) {
            alertDialog.dismiss();
        } else {
            Helpers.dismissProgressDialog();
        }
    }

    private class ViewHolder {
        private TextView spinnerText;
    }

    private class SpecialitiesAdapter extends BaseAdapter {

        private ViewHolder viewHolder;
        private ArrayList<Specialities> specialities;

        public SpecialitiesAdapter(ArrayList<Specialities> specialities) {
            this.specialities = specialities;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.delegate_spinner, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.spinnerText = (TextView) convertView.findViewById(R.id.spinner_text);
                viewHolder.spinnerText.setTypeface(AppGlobals.typefaceNormal);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            Specialities speciality = specialities.get(position);
            viewHolder.spinnerText.setText(speciality.getSpeciality());
            Log.i("TAF", speciality.getSpeciality());
            return convertView;
        }

        @Override
        public int getCount() {
            return specialities.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }
    }
}
