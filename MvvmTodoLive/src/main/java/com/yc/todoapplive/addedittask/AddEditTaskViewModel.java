

package com.yc.todoapplive.addedittask;

import com.yc.todoapplive.Event;
import com.yc.todoapplive.R;
import com.yc.todoapplive.data.Task;
import com.yc.todoapplive.data.source.TasksDataSource;
import com.yc.todoapplive.data.source.TasksRepository;

import androidx.annotation.Nullable;
import androidx.databinding.ObservableField;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * ViewModel for the Add/Edit screen.
 * <p>
 * This ViewModel only exposes {@link ObservableField}s, so it doesn't need to extend
 * {@link androidx.databinding.BaseObservable} and updates are notified automatically. See
 * {@link com.yc.todoapplive.statistics.StatisticsViewModel} for
 * how to deal with more complex scenarios.
 */
public class AddEditTaskViewModel extends ViewModel implements TasksDataSource.GetTaskCallback {

    // Two-way databinding, exposing MutableLiveData
    public final MutableLiveData<String> title = new MutableLiveData<>();

    // Two-way databinding, exposing MutableLiveData
    public final MutableLiveData<String> description = new MutableLiveData<>();

    private final MutableLiveData<Boolean> dataLoading = new MutableLiveData<>();

    private final MutableLiveData<Event<Integer>> mSnackbarText = new MutableLiveData<>();

    private final MutableLiveData<Event<Object>> mTaskUpdated = new MutableLiveData<>();

    private final TasksRepository mTasksRepository;

    @Nullable
    private String mTaskId;

    private boolean mIsNewTask;

    private boolean mIsDataLoaded = false;

    private boolean mTaskCompleted = false;

    public AddEditTaskViewModel(TasksRepository tasksRepository) {
        mTasksRepository = tasksRepository;
    }

    public void start(String taskId) {
        if (dataLoading.getValue() != null && dataLoading.getValue()) {
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
        dataLoading.setValue(true);

        mTasksRepository.getTask(taskId, this);
    }

    @Override
    public void onTaskLoaded(Task task) {
        title.setValue(task.getTitle());
        description.setValue(task.getDescription());
        mTaskCompleted = task.isCompleted();
        dataLoading.setValue(false);
        mIsDataLoaded = true;
    }

    @Override
    public void onDataNotAvailable() {
        dataLoading.setValue(false);
    }

    // Called when clicking on fab.
    void saveTask() {
        Task task = new Task(title.getValue(), description.getValue());
        if (task.isEmpty()) {
            mSnackbarText.setValue(new Event<>(R.string.empty_task_message));
            return;
        }
        if (isNewTask() || mTaskId == null) {
            createTask(task);
        } else {
            task = new Task(title.getValue(), description.getValue(), mTaskId, mTaskCompleted);
            updateTask(task);
        }
    }

    public LiveData<Event<Integer>> getSnackbarMessage() {
        return mSnackbarText;
    }

    public LiveData<Event<Object>> getTaskUpdatedEvent() {
        return mTaskUpdated;
    }

    public LiveData<Boolean> getDataLoading() {
        return dataLoading;
    }

    private boolean isNewTask() {
        return mIsNewTask;
    }

    private void createTask(Task newTask) {
        mTasksRepository.saveTask(newTask);
        mTaskUpdated.setValue(new Event<>(new Object()));
    }

    private void updateTask(Task task) {
        if (isNewTask()) {
            throw new RuntimeException("updateTask() was called but task is new.");
        }
        mTasksRepository.saveTask(task);
        mTaskUpdated.setValue(new Event<>(new Object()));
    }
}
