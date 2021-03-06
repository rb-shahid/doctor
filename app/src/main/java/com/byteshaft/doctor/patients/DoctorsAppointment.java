package com.byteshaft.doctor.patients;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.byteshaft.doctor.R;
import com.byteshaft.doctor.adapters.DiagnosticsAdapter;
import com.byteshaft.doctor.adapters.TargetsAdapter;
import com.byteshaft.doctor.adapters.TreatmentsAdapter;
import com.byteshaft.doctor.gettersetter.Diagnostics;
import com.byteshaft.doctor.gettersetter.Targets;
import com.byteshaft.doctor.gettersetter.Treatments;
import com.byteshaft.doctor.utils.AppGlobals;
import com.byteshaft.doctor.utils.Helpers;
import com.byteshaft.requests.HttpRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by husnain on 2/23/17.
 */

public class DoctorsAppointment extends AppCompatActivity implements View.OnClickListener,
        DatePickerDialog.OnDateSetListener {

    private Spinner mDiagnosticsSpinner;
    private Spinner mMedicationSpinner;
    private Spinner mDestinationSpinner;

    private EditText mDateEditText;
    private EditText mTimeEditText;
    private EditText mReturnDateEditText;
    private EditText mExplanationEditText;
    private EditText mConclusionsEditText;

    private DatePickerDialog datePickerDialog;
    private TimePickerDialog mTimePickerDialog;

    private boolean isSetForReturn = false;

    private View view;
    private ImageButton backPress;
    private TextView mPatientsName;
    private TextView mPatientsAge;
    private TextView mAppointmentReason;

    private String mFname;
    private String mLname;
    private String mAge;
    private String mReason;
    private String mDate;

    private ArrayList<Diagnostics> diagnosticsesList;
    private DiagnosticsAdapter diagnosticsAdapter;

    private ArrayList<Treatments> treatmentsArrayList;
    private TreatmentsAdapter treatmentsAdapter;

    private ArrayList<Targets> targetsArrayList;
    private TargetsAdapter targetsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(R.string.my_patient_details);
        setContentView(R.layout.activity_doctors_appointment);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.custom_action_bar);
        view = (View) findViewById(R.id.layout_for_name);
        mPatientsName = (TextView) view.findViewById(R.id.action_bar_title);
        mPatientsAge = (TextView) view.findViewById(R.id.action_bar_age);
        mAppointmentReason = (TextView) findViewById(R.id.appointment_reason);
        mDiagnosticsSpinner = (Spinner) findViewById(R.id.diagnostics_spinner);
        mMedicationSpinner = (Spinner) findViewById(R.id.medication_spinner);
        mDestinationSpinner = (Spinner) findViewById(R.id.destination_spinner);

        mDateEditText = (EditText) findViewById(R.id.date_edit_text);
        mTimeEditText = (EditText) findViewById(R.id.time_edit_text);
        mReturnDateEditText = (EditText) findViewById(R.id.return_date_edit_text);
        mExplanationEditText = (EditText) findViewById(R.id.explanation_edit_text);
        mConclusionsEditText = (EditText) findViewById(R.id.conclusions_edit_text);
        backPress = (ImageButton) findViewById(R.id.back_press);
        backPress.setOnClickListener(this);

        mDateEditText.setTypeface(AppGlobals.typefaceNormal);
        mTimeEditText.setTypeface(AppGlobals.typefaceNormal);
        mReturnDateEditText.setTypeface(AppGlobals.typefaceNormal);
        mExplanationEditText.setTypeface(AppGlobals.typefaceNormal);
        mConclusionsEditText.setTypeface(AppGlobals.typefaceNormal);

        mReturnDateEditText.setOnClickListener(this);


        mFname = getIntent().getStringExtra("first_name");
        mLname = getIntent().getStringExtra("last_name");
        mAge = getIntent().getStringExtra("age");
        mReason = getIntent().getStringExtra("reason");
        mDate = getIntent().getStringExtra("date");

        mPatientsName.setText(mFname + " " + mLname);
        String years = Helpers.calculateAge(mAge);
        mPatientsAge.setText(years + " " + "years");
        mAppointmentReason.setText(mReason);
        mDateEditText.setText(mDate);
        mTimeEditText.setText(Helpers.getTime24HourFormat());
        mTimeEditText.setEnabled(false);
        mDateEditText.setEnabled(false);

        diagnosticsesList = new ArrayList<>();
        treatmentsArrayList = new ArrayList<>();
        targetsArrayList = new ArrayList<>();
        getDiagnostic();
        getTreamants();
        getTargets();



        final Calendar calendar = Calendar.getInstance();
        datePickerDialog = new DatePickerDialog(DoctorsAppointment.this,
                this,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        mTimePickerDialog = new TimePickerDialog(DoctorsAppointment.this,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        mTimeEditText.setText(convertDate(hourOfDay) + ":" + convertDate(minute));

                    }
                }, calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE), false);
    }


    public String convertDate(int input) {
        if (input >= 10) {
            return String.valueOf(input);
        } else {
            return "0" + String.valueOf(input);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.return_date_edit_text:
                datePickerDialog.show();
                break;
            case R.id.back_press:
                onBackPressed();
                break;
        }
    }


    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
        if (!isSetForReturn) {
            mDateEditText.setText(i2 + "/" + i1 + "/" + i);
            isSetForReturn = true;
        } else {
            mReturnDateEditText.setText(i2 + "/" + i1 + "/" + i);
            isSetForReturn = false;
        }


    }

    private void getDiagnostic() {
        HttpRequest diagnosticsRequest = new HttpRequest(this);
        diagnosticsRequest.setOnReadyStateChangeListener(new HttpRequest.OnReadyStateChangeListener() {
            @Override
            public void onReadyStateChange(HttpRequest request, int readyState) {
                switch (readyState) {
                    case HttpRequest.STATE_DONE:
                        switch (request.getStatus()) {
                            case HttpURLConnection.HTTP_OK:
                                try {
                                    JSONObject diagnosticsObject = new JSONObject(request.getResponseText());
                                    JSONArray diagnosticsArray = diagnosticsObject.getJSONArray("results");
                                    System.out.println(diagnosticsArray + "working");
                                    for (int i = 0; i < diagnosticsArray.length(); i++) {
                                        JSONObject jsonObject = diagnosticsArray.getJSONObject(i);
                                        Diagnostics diagnostics = new Diagnostics();
                                        diagnostics.setId(jsonObject.getInt("id"));
                                        diagnostics.setName(jsonObject.getString("name"));
                                        diagnosticsesList.add(diagnostics);
                                    }
                                    System.out.println(diagnosticsArray.length() + "length");
                                    diagnosticsAdapter = new DiagnosticsAdapter(DoctorsAppointment.this, diagnosticsesList);
                                    mDiagnosticsSpinner.setAdapter(diagnosticsAdapter);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                        }
                }
            }
        });

        diagnosticsRequest.open("GET", String.format("%sdiagnostics/", AppGlobals.BASE_URL));
        diagnosticsRequest.send();
    }

    private void getTreamants() {
        HttpRequest diagnosticsRequest = new HttpRequest(this);
        diagnosticsRequest.setOnReadyStateChangeListener(new HttpRequest.OnReadyStateChangeListener() {
            @Override
            public void onReadyStateChange(HttpRequest request, int readyState) {
                switch (readyState) {
                    case HttpRequest.STATE_DONE:
                        switch (request.getStatus()) {
                            case HttpURLConnection.HTTP_OK:
                                try {
                                    JSONObject treatmentsObject = new JSONObject(request.getResponseText());
                                    JSONArray treatmentsArray = treatmentsObject.getJSONArray("results");
                                    System.out.println(treatmentsArray + "working");
                                    for (int i = 0; i < treatmentsArray.length(); i++) {
                                        JSONObject jsonObject = treatmentsArray.getJSONObject(i);
                                        Treatments treatments = new Treatments();
                                        treatments.setId(jsonObject.getInt("id"));
                                        treatments.setName(jsonObject.getString("name"));
                                        treatmentsArrayList.add(treatments);
                                    }
                                    System.out.println(treatmentsArray.length() + "length");
                                    treatmentsAdapter = new TreatmentsAdapter(DoctorsAppointment.this, treatmentsArrayList);
                                    mMedicationSpinner.setAdapter(treatmentsAdapter);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                        }
                }
            }
        });

        diagnosticsRequest.open("GET", String.format("%streatments/", AppGlobals.BASE_URL));
        diagnosticsRequest.send();
    }

    private void getTargets() {
        HttpRequest diagnosticsRequest = new HttpRequest(this);
        diagnosticsRequest.setOnReadyStateChangeListener(new HttpRequest.OnReadyStateChangeListener() {
            @Override
            public void onReadyStateChange(HttpRequest request, int readyState) {
                switch (readyState) {
                    case HttpRequest.STATE_DONE:
                        switch (request.getStatus()) {
                            case HttpURLConnection.HTTP_OK:
                                try {
                                    JSONObject targetsObject = new JSONObject(request.getResponseText());
                                    JSONArray targetsArray = targetsObject.getJSONArray("results");
                                    System.out.println(targetsArray + "working");
                                    for (int i = 0; i < targetsArray.length(); i++) {
                                        JSONObject jsonObject = targetsArray.getJSONObject(i);
                                        Targets targets = new Targets();
                                        targets.setId(jsonObject.getInt("id"));
                                        targets.setName(jsonObject.getString("name"));
                                        targetsArrayList.add(targets);
                                    }
                                    System.out.println(targetsArray.length() + "length");
                                    targetsAdapter = new TargetsAdapter(DoctorsAppointment.this, targetsArrayList);
                                    mDestinationSpinner.setAdapter(targetsAdapter);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                        }
                }
            }
        });

        diagnosticsRequest.open("GET", String.format("%stargets/", AppGlobals.BASE_URL));
        diagnosticsRequest.send();
    }
}
