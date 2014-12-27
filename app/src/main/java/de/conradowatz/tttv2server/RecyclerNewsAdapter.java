package de.conradowatz.tttv2server;

import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class RecyclerNewsAdapter extends RecyclerView.Adapter<RecyclerNewsAdapter.ProfileHolder> {

    private String[] titleArray;
    private String[] dateArray;
    private String[] contentArray;

    public RecyclerNewsAdapter(String[] titles, String[] dates, String[] contents) {
        titleArray = titles;
        dateArray = dates;
        contentArray = contents;

        for (int i=0; i<dateArray.length; i++) {
            if (!dateArray[i].startsWith("test")) {
                SimpleDateFormat format =
                        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", new DateFormatSymbols(Locale.US));

                Date date = null;
                try {
                    date = format.parse(dateArray[i]);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd. MMM yyyy HH:mm");
                dateArray[i] = outputFormat.format(date);
            }
        }
    }

    @Override
    public ProfileHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_news_item, parent, false);

        return new ProfileHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ProfileHolder holder, final int position) {

        holder.vTitle.setText(titleArray[position]);
        holder.vDate.setText("Veröffentlicht am " + dateArray[position]);
        contentArray[position] = contentArray[position].replaceAll("<li>", "• "); //handle dots
        contentArray[position] = contentArray[position].replaceAll("\n", "<div>"); //handle linebreaks
        contentArray[position] = contentArray[position].replaceAll("\\<img(.*?)\\>", ""); //dont show images
        contentArray[position] = contentArray[position].replaceAll("\\<table(.*?)table\\>", ""); //dont show tables
        holder.vContent.setText(Html.fromHtml(contentArray[position]));

    }



    @Override
    public int getItemCount() {
        return titleArray.length;
    }


    public class ProfileHolder extends RecyclerView.ViewHolder {
        protected TextView vTitle;
        protected TextView vDate;
        protected TextView vContent;


        public ProfileHolder(View itemView) {
            super(itemView);
            vTitle = (TextView) itemView.findViewById(R.id.textView_news_title);
            vDate = (TextView) itemView.findViewById(R.id.textView_news_date);
            vContent = (TextView) itemView.findViewById(R.id.textView_news_content);

        }

    }
}