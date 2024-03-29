

package com.yc.todoappmvvm.addedittask;

import android.content.Context;


import androidx.annotation.Nullable;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;

import com.yc.todoappmvvm.R;
import com.yc.todoappmvvm.data.Task;
import com.yc.todoappmvvm.data.source.TasksDataSource;
import com.yc.todoappmvvm.data.source.TasksRepository;

/**
 * ViewModel for the Add/Edit screen.
 * <p>
 * This ViewModel only exposes {@link ObservableField}s, so it doesn't need to extend
 * {@link androidx.databinding.BaseObservable} and updates are notified automatically. See
 * {@link com.yc.todoappmvvm.statistics.StatisticsViewModel} for
 * how to deal with more complex scenarios.
 */
public class AddEditTaskViewModel implements TasksDataSource.GetTaskCallback {

    public final ObservableField<String> title = new ObservableField<>();

    public final ObservableField<String> description = new ObservableField<>();

    public final ObservableBoolean dataLoading = new ObservableBoolean(false);

    public final ObservableField<String> snackbarText = new ObservableField<>();

    private final TasksRepository mTasksRepository;

    private final Context mContext;  // To avoid leaks, this must be an Application Context.

    @Nullable
    private String mTaskId;

    private boolean mIsNewTask;

    private boolean mIsDataLoaded = false;

    private AddEditTaskNavigator mAddEditTaskNavigator;

    AddEditTaskViewModel(Context context, TasksRepository tasksRepository) {
        mContext = context.getApplicationContext(); // Force use of Application Context.
        mTasksRepository = tasksRepository;
    }

    void onActivityCreated(AddEditTaskNavigator navigator) {
        mAddEditTaskNavigator = navigator;
    }

    void onActivityDestroyed() {
        // Clear references to avoid potential memory leaks.
        mAddEditTaskNavigator = null;
    }

    public void start(String taskId) {
        if (dataLoading.get()) {
            // Already loading, ignore.
            return;
        }
        mTaskId = taskId;
        if (taskId == null) {
            // No need to populate, it's a new task
            mIsNewTask = true;
            return;
        }
        if (mIsDataLoaded) {
            // No need to populate, already have data.
            return;
        }
        mIsNewTask = false;
        dataLoading.set(true);
        mTasksRepository.getTask(taskId, this);
    }

    @Override
    public void onTaskLoaded(Task task) {
        title.set(task.getTitle());
        description.set(task.getDescription());
        dataLoading.set(false);
        mIsDataLoaded = true;

        // Note that there's no need to notify that the values changed because we're using
        // ObservableFields.
    }

    @Override
    public void onDataNotAvailable() {
        dataLoading.set(false);
    }

    // Called when clicking on fab.
    public void saveTask() {
        if (isNewTask()) {
            createTask(title.get(), description.get());
        } else {
            updateTask(title.get(), description.get());
        }
    }

    @Nullable
    public String getSnackbarText() {
        return snackbarText.get();
    }

    private boolean isNewTask() {
        return mIsNewTask;
    }

    private void createTask(String title, String description) {
        Task newTask = new Task(title, description);
        if (newTask.isEmpty()) {
            snackbarText.set(mContext.getString(R.string.empty_task_message));
        } else {
            mTasksRepository.saveTask(newTask);
            navigateOnTaskSaved();
        }
    }

    private void updateTask(String title, String description) {
        if (isNewTask()) {
            throw new RuntimeException("updateTask() was called but task is new.");
        }
        mTasksRepository.saveTask(new Task(title, description, mTaskId));
        navigateOnTaskSaved(); // After an edit, go back to the list.
    }

    private void navigateOnTaskSaved() {
        if (mAddEditTaskNavigator!= null) {
            mAddEditTaskNavigator.onTaskSaved();
        }
    }
}
