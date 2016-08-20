/*
 * Copyright (c) 2016 Jan Böhmer
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jbtronic.recolldroid;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.text.Html;
import android.view.LayoutInflater;


import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.VolleyError;

import jbtronic.recolldroid.dummy.DummyContent;
import jbtronics.recolldroid.api.Connection;
import jbtronics.recolldroid.api.Query;
import jbtronics.recolldroid.api.Result;

import java.util.List;

/**
 * An activity representing a list of Results. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ResultDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class ResultListActivity extends AppCompatActivity
    implements Connection.onQueryError{

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
                //Connection con = new Connection("http://ras.pi/recoll",view.getContext());
                //con.query("test");


            }
        });

        handleIntent(getIntent());

        //View recyclerView = findViewById(R.id.result_list);
        //assert recyclerView != null;
        //setupRecyclerView((RecyclerView) recyclerView);

        if (findViewById(R.id.result_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView,Query q) {
        recyclerView.setAdapter(new ResultItemRecyclerViewAdapter(q));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);

            final ProgressBar progressBar = (ProgressBar)  findViewById(R.id.progress_bar);
            final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.result_list);

            progressBar.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);


            Connection conn = new Connection("http://ras.pi/recoll",this);
            conn.setOnCompleteListener(new Connection.onQueryComplete() {
                @Override
                public void querycompleted(Query q) {
                    setupRecyclerView(recyclerView, q);
                    progressBar.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            });

            conn.query(query);

        }
    }

    @Override
    public void onBackPressed() {
        SearchView searchView = (SearchView) findViewById(R.id.action_search);
        if (!searchView.isIconified()) {
            searchView.setIconified(true);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void queryerror(VolleyError error) {
        Toast t = Toast.makeText(this,"Error loading results!",Toast.LENGTH_SHORT);
    }


    public class ResultItemRecyclerViewAdapter
            extends RecyclerView.Adapter<ResultItemRecyclerViewAdapter.ViewHolder> {

        private Query query;

        public ResultItemRecyclerViewAdapter(Query query) {
            this.query = query;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.result_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            Result r = query.getResult(position);
            holder.mResult = r;
            holder.mTitle.setText(r.getHeader());
            holder.mFolder.setText(r.getFolder());
            holder.mPreview.setText(Html.fromHtml(r.getFormattedSnippet())); //Other methods are API 24 or higher.

            if(r.getIpath().equals(""))
            {
                holder.mIpath.setVisibility(View.GONE);
            }
            else
            {
                holder.mIpath.setText(r.getIpath());
            }

            //holder.mContentView.setText(mValues.get(position).content);

            /*
            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putString(ResultDetailFragment.ARG_ITEM_ID, holder.mItem.id);
                        ResultDetailFragment fragment = new ResultDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.result_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, ResultDetailActivity.class);
                        intent.putExtra(ResultDetailFragment.ARG_ITEM_ID, holder.mItem.id);

                        context.startActivity(intent);
                    }
                }
            });
            */
        }

        @Override
        public int getItemCount() {
            return query.getResults().size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public View mView;
            public TextView mTitle;
            public TextView mPreview;
            public TextView mFolder;
            public TextView mIpath;
            public Result mResult;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mTitle = (TextView) view.findViewById(R.id.result_list_title);
                mFolder = (TextView) view.findViewById(R.id.result_list_folder);
                mPreview = (TextView) view.findViewById(R.id.result_list_preview);
                mIpath = (TextView) view.findViewById(R.id.result_list_ipath);
                mResult = null;
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mTitle.getText() + "'";
            }
        }
    }
}
