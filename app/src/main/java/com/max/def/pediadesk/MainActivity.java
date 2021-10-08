package com.max.def.pediadesk;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.google.common.base.Joiner;
import com.max.def.pediadesk.Utils.GoogleWebSearch;
import com.max.def.pediadesk.Utils.PermissionUtil;
import com.max.def.pediadesk.Utils.SearchQuery;
import com.max.def.pediadesk.Utils.SearchResult;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity
{
    private PermissionUtil permissionUtil;

    private AppCompatEditText queryEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        permissionUtil = new PermissionUtil(this);

        if (checkPermission(PermissionUtil.READ_INTERNET) != PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,android.Manifest.permission.INTERNET))
            {
                showPermissionExplanation(PermissionUtil.READ_INTERNET);
            }
            else if (permissionUtil.checkPermissionPreference(PermissionUtil.PERMISSION_INTERNET))
            {
                requestPermission(PermissionUtil.READ_INTERNET);
                permissionUtil.updatePermissionPreference(PermissionUtil.PERMISSION_INTERNET);
            }
            else
            {
                Toast.makeText(MainActivity.this, "Please allow internet permission in your app settings", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package",MainActivity.this.getPackageName(),null);
                intent.setData(uri);
                this.startActivity(intent);
            }
        }

        Toolbar mainToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);

        Paper.init(this);

        queryEditText = findViewById(R.id.query_edit_text);
        AppCompatButton searchQueryBtn = findViewById(R.id.search_query_btn);
        final AppCompatTextView resultView = findViewById(R.id.result_text_view);

        searchQueryBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String query = queryEditText.getText().toString();

                if (query.equals("") || query.equals(" "))
                {
                    Toast.makeText(MainActivity.this, "Please enter any title...", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    queryEditText.setText("");

                    try
                    {
                        Document document = Jsoup.connect("https://en.wikipedia.org/wiki/" + query).get();

                        Elements paragraphs = document.select("p:not(:has(#coordinates))");

                        if (paragraphs.size() > 0)
                        {
                            StringBuilder stringBuilder = new StringBuilder();

                            for (int i=0; i<paragraphs.size(); i++)
                            {
                                Element first = paragraphs.get(i);

                                String definition = first.text();

                                stringBuilder.append(definition);
                            }

                            String definitionForQuery = stringBuilder.toString();

                            if (!definitionForQuery.equals("null") && !definitionForQuery.equals(""))
                            {
                                String text = "Definition of " + query + " : " + definitionForQuery;

                                resultView.setText(text);
                            }
                            else
                            {
                                resultView.setText(failureResponseForName(query));
                            }
                        }
                        else
                        {
                            SearchQuery searchQuery = new SearchQuery.Builder(query).site("en.wikipedia.org").numResults(2).build();

                            SearchResult result = new GoogleWebSearch().search(searchQuery);

                            List<String> url = result.getUrls();

                            if (url != null && url.size() > 0)
                            {
                                Document finalDocument = Jsoup.connect(url.get(0)).get();

                                Elements finalParagraphs = finalDocument.select("p:not(:has(#coordinates))");

                                if (finalParagraphs.size() > 0)
                                {
                                    StringBuilder stringBuilder = new StringBuilder();

                                    for (int i=0; i<paragraphs.size(); i++)
                                    {
                                        Element first = paragraphs.get(i);

                                        String definition = first.text();

                                        stringBuilder.append(definition);
                                    }

                                    String definitionForQuery = stringBuilder.toString();

                                    if (!definitionForQuery.equals("null") && !definitionForQuery.equals(""))
                                    {
                                        String text = "Definition of " + query + " : " + definitionForQuery;

                                        resultView.setText(text);
                                    }
                                    else
                                    {
                                        resultView.setText(failureResponseForName(query));
                                    }
                                }
                                else
                                {
                                    resultView.setText(failureResponseForName(query));
                                }
                            }
                            else
                            {
                                resultView.setText(failureResponseForName(query));
                            }
                        }
                    }
                    catch (IOException e)
                    {
                        resultView.setText(failureResponseForName(query));
                    }
                }
            }
        });
    }

    private String failureResponseForName(String query)
    {
        SearchQuery wikiQuery = new SearchQuery.Builder(query).site("en.wikipedia.org").numResults(10).build();

        SearchResult wikiResult = new GoogleWebSearch().search(wikiQuery);

        ArrayList<String> urls = new ArrayList<>(wikiResult.getUrls());

        SearchQuery quoraQuery = new SearchQuery.Builder(query).site("quora.com").numResults(10).build();

        SearchResult quoraResult = new GoogleWebSearch().search(quoraQuery);

        urls.addAll(quoraResult.getUrls());

        SearchQuery stackQuery = new SearchQuery.Builder(query).site("stackoverflow.com").numResults(10).build();

        SearchResult stackResult = new GoogleWebSearch().search(stackQuery);

        urls.addAll(stackResult.getUrls());

        String listString = Joiner.on(", \n").join(urls);

        return "I could not find the definition for " + query + ", \nPlease go through the below best websites.\n" + listString + " Please, try another name to get the definition.";
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        if (checkPermission(PermissionUtil.READ_INTERNET) != PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,android.Manifest.permission.INTERNET))
            {
                showPermissionExplanation(PermissionUtil.READ_INTERNET);
            }
            else if (permissionUtil.checkPermissionPreference(PermissionUtil.PERMISSION_INTERNET))
            {
                requestPermission(PermissionUtil.READ_INTERNET);
                permissionUtil.updatePermissionPreference(PermissionUtil.PERMISSION_INTERNET);
            }
            else
            {
                Toast.makeText(MainActivity.this, "Please allow internet permission in your app settings", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package",MainActivity.this.getPackageName(),null);
                intent.setData(uri);
                this.startActivity(intent);
            }
        }

    }

    private int checkPermission(int permission)
    {
        int status = PackageManager.PERMISSION_DENIED;

        switch (permission)
        {
            case PermissionUtil.READ_INTERNET:
                status = ContextCompat.checkSelfPermission(this,android.Manifest.permission.INTERNET);
                break;
        }
        return status;
    }

    private void requestPermission(int permission)
    {
        switch (permission)
        {
            case PermissionUtil.READ_INTERNET:
                ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.INTERNET},PermissionUtil.REQUEST_INTERNET);
                break;
        }
    }

    private void showPermissionExplanation(final int permission)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        switch (permission)
        {
            case PermissionUtil.READ_INTERNET:
                builder.setMessage("This app need to access your internet..");
                builder.setTitle("Internet Permission Needed..");
                break;
        }

        builder.setPositiveButton("Allow", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                switch (permission)
                {
                    case PermissionUtil.READ_INTERNET:
                        requestPermission(PermissionUtil.READ_INTERNET);
                        break;
                }
            }
        });

        builder.setNegativeButton("Deny", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
