

package com.yc.todoapplive.tasks;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.yc.todoapplive.Event;
import com.yc.todoapplive.R;
import com.yc.todoapplive.ScrollChildSwipeRefreshLayout;
import com.yc.todoapplive.data.Task;
import com.yc.todoapplive.databinding.TasksFragBinding;
import com.yc.todoapplive.util.SnackbarUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

/**
 * Display a grid of {@link Task}s. User can choose to view all, active or completed tasks.
 */
public class TasksFragment extends Fragment {

    private TasksViewModel mTasksViewModel;

    private TasksFragBinding mTasksFragBinding;

    private TasksAdapter mListAdapter;

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

        mTasksViewModel = TasksLiveActivity.obtainViewModel(getActivity());

        mTasksFragBinding.setViewmodel(mTasksViewModel);
        mTasksFragBinding.setLifecycleOwner(getActivity());

        setHasOptionsMenu(true);

        return mTasksFragBinding.getRoot();
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

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setupSnackbar();

        setupFab();

        setupListAdapter();

        setupRefreshLayout();
    }

    private void setupSnackbar() {
        mTasksViewModel.getSnackbarMessage().observe(this, new Observer<Event<Integer>>() {
            @Override
            public void onChanged(Event<Integer> event) {
                Integer msg = event.getContentIfNotHandled();
                if (msg != null) {
                    SnackbarUtils.showSnackbar(getView(), getString(msg));
                }
            }
        });
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
        FloatingActionButton fab = getActivity().findViewById(R.id.fab_add_task);

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
                mTasksViewModel,
                getActivity()
        );
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

}
