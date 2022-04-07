

package com.yc.todoapplive.statistics;

import com.yc.todoapplive.data.Task;
import com.yc.todoapplive.data.source.TasksDataSource;
import com.yc.todoapplive.data.source.TasksRepository;

import java.util.List;

import androidx.databinding.Bindable;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * Exposes the data to be used in the statistics screen.
 * <p>
 * This ViewModel uses both {@link ObservableField}s ({@link ObservableBoolean}s in this case) and
 * {@link Bindable} getters. The values in {@link ObservableField}s are used directly in the layout,
 * whereas the {@link Bindable} getters allow us to add some logic to it. This is
 * preferable to having logic in the XML layout.
 */
public class StatisticsViewModel extends ViewModel {

    private final MutableLiveData<Boolean> mDataLoading = new MutableLiveData<>();

    private final MutableLiveData<Boolean> mError = new MutableLiveData<>();

    private final MutableLiveData<Integer> mActiveTasks = new MutableLiveData<>();

    private final MutableLiveData<Integer> mCompletedTasks = new MutableLiveData<>();

    private final MutableLiveData mEmpty = new MutableLiveData();

    private int mNumberOfActiveTasks = 0;

    private int mNumberOfCompletedTasks = 0;

    private final TasksRepository mTasksRepository;

    public StatisticsViewModel(TasksRepository tasksRepository) {
        mTasksRepository = tasksRepository;
    }

    public void start() {
        loadStatistics();
    }

    public void loadStatistics() {
        mDataLoading.setValue(true);

        mTasksRepository.getTasks(new TasksDataSource.LoadTasksCallback() {
            @Override
            public void onTasksLoaded(List<Task> tasks) {
                mError.setValue(false);
                computeStats(tasks);
            }

            @Override
            public void onDataNotAvailable() {
                mError.setValue(true);
                mNumberOfActiveTasks = 0;
                mNumberOfCompletedTasks = 0;
                updateDataBindingObservables();
            }
        });
    }

    // LiveData getters

    public LiveData<Boolean> getDataLoading() {
        return mDataLoading;
    }

    public LiveData<Boolean> getError() {
        return mError;
    }

    public MutableLiveData<Integer> getNumberOfActiveTasks() {
        return mActiveTasks;
    }

    public MutableLiveData<Integer> getNumberOfCompletedTasks() {
        return mCompletedTasks;
    }

    /**
     * Controls whether the stats are shown or a "No data" message.
     */
    public LiveData<Boolean> getEmpty() {
        return mEmpty;
    }

    /**
     * Called when new data is ready.
     */
    private void computeStats(List<Task> tasks) {
        int completed = 0;
        int active = 0;

        for (Task task : tasks) {
            if (task.isCompleted()) {
                completed += 1;
            } else {
                active += 1;
            }
        }
        mNumberOfActiveTasks = active;
        mNumberOfCompletedTasks = completed;

        updateDataBindingObservables();
    }

    private void updateDataBindingObservables() {
        mCompletedTasks.setValue(mNumberOfCompletedTasks);
        mActiveTasks.setValue(mNumberOfActiveTasks);
        mEmpty.setValue(mNumberOfActiveTasks + mNumberOfCompletedTasks == 0);
        mDataLoading.setValue(false);

    }
}
