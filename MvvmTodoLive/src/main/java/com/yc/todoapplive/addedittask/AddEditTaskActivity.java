

package com.yc.todoapplive.addedittask;

import android.os.Bundle;

import com.yc.todoapplive.Event;
import com.yc.todoapplive.R;
import com.yc.todoapplive.ViewModelFactory;
import com.yc.todoapplive.util.ActivityUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

/**
 * Displays an add or edit task screen.
 */
public class AddEditTaskActivity extends AppCompatActivity implements AddEditTaskNavigator {

    public static final int REQUEST_CODE = 1;

    public static final int ADD_EDIT_RESULT_OK = RESULT_FIRST_USER + 1;

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onTaskSaved() {
        setResult(ADD_EDIT_RESULT_OK);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addtask_act);

        // Set up the toolbar.
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        AddEditTaskFragment addEditTaskFragment = obtainViewFragment();

        ActivityUtils.replaceFragmentInActivity(getSupportFragmentManager(),
                addEditTaskFragment, R.id.contentFrame);

        subscribeToNavigationChanges();
    }

    private void subscribeToNavigationChanges() {
        AddEditTaskViewModel viewModel = obtainViewModel(this);

        // The activity observes the navigation events in the ViewModel
        viewModel.getTaskUpdatedEvent().observe(this, new Observer<Event<Object>>() {
            @Override
            public void onChanged(Event<Object> taskIdEvent) {
                if (taskIdEvent.getContentIfNotHandled() != null) {
                    AddEditTaskActivity.this.onTaskSaved();
                }
            }
        });
    }

    public static AddEditTaskViewModel obtainViewModel(FragmentActivity activity) {
        // Use a Factory to inject dependencies into the ViewModel
        ViewModelFactory factory = ViewModelFactory.getInstance(activity.getApplication());

        return ViewModelProviders.of(activity, factory).get(AddEditTaskViewModel.class);
    }

    @NonNull
    private AddEditTaskFragment obtainViewFragment() {
        // View Fragment
        AddEditTaskFragment addEditTaskFragment = (AddEditTaskFragment) getSupportFragmentManager()
                .findFragmentById(R.id.contentFrame);

        if (addEditTaskFragment == null) {
            addEditTaskFragment = AddEditTaskFragment.newInstance();

            // Send the task ID to the fragment
            Bundle bundle = new Bundle();
            bundle.putString(AddEditTaskFragment.ARGUMENT_EDIT_TASK_ID,
                    getIntent().getStringExtra(AddEditTaskFragment.ARGUMENT_EDIT_TASK_ID));
            addEditTaskFragment.setArguments(bundle);
        }
        return addEditTaskFragment;
    }
}
