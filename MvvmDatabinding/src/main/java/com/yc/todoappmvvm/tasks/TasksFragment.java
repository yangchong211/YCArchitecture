

package com.yc.todoappmvvm.tasks;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.Observable;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.yc.todoappmvvm.Injection;
import com.yc.todoappmvvm.R;
import com.yc.todoappmvvm.ScrollChildSwipeRefreshLayout;
import com.yc.todoappmvvm.data.Task;
import com.yc.todoappmvvm.data.source.TasksRepository;
import com.yc.todoappmvvm.databinding.TaskItemBinding;
import com.yc.todoappmvvm.databinding.TasksFragBinding;
import com.yc.todoappmvvm.util.SnackbarUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Display a grid of {@link Task}s. User can choose to view all, active or completed tasks.
 */
public class TasksFragment extends Fragment {

    private TasksViewModel mTasksViewModel;

    private TasksFragBinding mTasksFragBinding;

    private TasksAdapter mListAdapter;

    private Observable.OnPropertyChangedCallback mSnackbarCallback;

    public TasksFragment() {
        // Requires empty public constructor
    }

    public static TasksFragment newInstance() {
        return new TasksFragment();
    }

    @Override
    public void onResume() {
        super.onResume();
        mTasksViewModel.start();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mTasksFragBinding = TasksFragBinding.inflate(inflater, container, false);

        mTasksFragBinding.setView(this);

        mTasksFragBinding.setViewmodel(mTasksViewModel);

        setHasOptionsMenu(true);

        View root = mTasksFragBinding.getRoot();

        return root;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_clear) {
            mTasksViewModel.clearCompletedTasks();
        } else if (itemId == R.id.menu_filter) {
            showFilteringPopUpMenu();
        } else if (itemId == R.id.menu_refresh) {
            mTasksViewModel.loadTasks(true);
        }
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.tasks_fragment_menu, menu);
    }

    public void setViewModel(TasksViewModel viewModel) {
        mTasksViewModel = viewModel;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setupSnackbar();

        setupFab();

        setupListAdapter();

        setupRefreshLayout();
    }

    @Override
    public void onDestroy() {
        mListAdapter.onDestroy();
        if (mSnackbarCallback != null) {
            mTasksViewModel.snackbarText.removeOnPropertyChangedCallback(mSnackbarCallback);
        }
        super.onDestroy();
    }

    private void setupSnackbar() {
        mSnackbarCallback = new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable observable, int i) {
                SnackbarUtils.showSnackbar(getView(), mTasksViewModel.getSnackbarText());
            }
        };
        mTasksViewModel.snackbarText.addOnPropertyChangedCallback(mSnackbarCallback);
    }

    private void showFilteringPopUpMenu() {
        PopupMenu popup = new PopupMenu(getContext(), getActivity().findViewById(R.id.menu_filter));
        popup.getMenuInflater().inflate(R.menu.filter_tasks, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.active) {
                    mTasksViewModel.setFiltering(TasksFilterType.ACTIVE_TASKS);
                } else if (itemId == R.id.completed) {
                    mTasksViewModel.setFiltering(TasksFilterType.COMPLETED_TASKS);
                } else {
                    mTasksViewModel.setFiltering(TasksFilterType.ALL_TASKS);
                }
                mTasksViewModel.loadTasks(false);
                return true;
            }
        });

        popup.show();
    }

    private void setupFab() {
        FloatingActionButton fab =
                (FloatingActionButton) getActivity().findViewById(R.id.fab_add_task);

        fab.setImageResource(R.drawable.ic_add);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTasksViewModel.addNewTask();
            }
        });
    }

    private void setupListAdapter() {
        ListView listView =  mTasksFragBinding.tasksList;

        mListAdapter = new TasksAdapter(
                new ArrayList<Task>(0),
                (TasksMvvmActivity) getActivity(),
                Injection.provideTasksRepository(getContext().getApplicationContext()),
                mTasksViewModel);
        listView.setAdapter(mListAdapter);
    }

    private void setupRefreshLayout() {
        ListView listView =  mTasksFragBinding.tasksList;
        final ScrollChildSwipeRefreshLayout swipeRefreshLayout = mTasksFragBinding.refreshLayout;
        swipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(getActivity(), R.color.colorPrimary),
                ContextCompat.getColor(getActivity(), R.color.colorAccent),
                ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark)
        );
        // Set the scrolling view in the custom SwipeRefreshLayout.
        swipeRefreshLayout.setScrollUpChild(listView);
    }

    public static class TasksAdapter extends BaseAdapter {

        @Nullable private TaskItemNavigator mTaskItemNavigator;

        private final TasksViewModel mTasksViewModel;

        private List<Task> mTasks;

        private TasksRepository mTasksRepository;

        public TasksAdapter(List<Task> tasks, TasksMvvmActivity taskItemNavigator,
                            TasksRepository tasksRepository,
                            TasksViewModel tasksViewModel) {
            mTaskItemNavigator = taskItemNavigator;
            mTasksRepository = tasksRepository;
            mTasksViewModel = tasksViewModel;
            setList(tasks);

        }

        public void onDestroy() {
            mTaskItemNavigator = null;
        }

        public void replaceData(List<Task> tasks) {
            setList(tasks);
        }

        @Override
        public int getCount() {
            return mTasks != null ? mTasks.size() : 0;
        }

        @Override
        public Task getItem(int i) {
            return mTasks.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            Task task = getItem(i);
            TaskItemBinding binding;
            if (view == null) {
                // Inflate
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());

                // Create the binding
                binding = TaskItemBinding.inflate(inflater, viewGroup, false);
            } else {
                // Recycling view
                binding = DataBindingUtil.getBinding(view);
            }

            final TaskItemViewModel viewmodel = new TaskItemViewModel(
                    viewGroup.getContext().getApplicationContext(),
                    mTasksRepository
            );

            viewmodel.setNavigator(mTaskItemNavigator);

            binding.setViewmodel(viewmodel);
            // To save on PropertyChangedCallbacks, wire the item's snackbar text observable to the
            // fragment's.
            viewmodel.snackbarText.addOnPropertyChangedCallback(
                    new Observable.OnPropertyChangedCallback() {
                @Override
                public void onPropertyChanged(Observable observable, int i) {
                    mTasksViewModel.snackbarText.set(viewmodel.getSnackbarText());
                }
            });
            viewmodel.setTask(task);

            return binding.getRoot();
        }


        private void setList(List<Task> tasks) {
            mTasks = tasks;
            notifyDataSetChanged();
        }
    }
}
