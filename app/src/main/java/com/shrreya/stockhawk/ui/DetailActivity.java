package com.shrreya.stockhawk.ui;

import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.shrreya.stockhawk.R;
import com.shrreya.stockhawk.data.Contract;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;



import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.shrreya.stockhawk.utils.Parser;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    @BindView(R.id.stock_name) public TextView tvStockName;
    @BindView(R.id.stock_price) public TextView tvStockPrice;
    @BindView(R.id.stock_change) public TextView tvStockChange;
    @BindView(R.id.highest) public TextView tvHighest;
    @BindView(R.id.lowest) public TextView tvLowest;
    @BindView(R.id.chart) public LineChart lineChart;
    @BindColor(R.color.white) public int white;

    private Uri stockUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        getSupportActionBar().setTitle("");

        ButterKnife.bind(this);

        stockUri = getIntent().getData();
        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(stockUri != null) {
            return new CursorLoader(this, stockUri, Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}), null, null, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data.moveToFirst()) {
            String stockName = data.getString(Contract.Quote.POSITION_NAME);
            Float stockPrice = data.getFloat(Contract.Quote.POSITION_PRICE);
            Float stockChange = data.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
            Float lowest = data.getFloat(Contract.Quote.POSITION_LOWEST);
            Float highest = data.getFloat(Contract.Quote.POSITION_HIGHEST);
            String historyData = data.getString(Contract.Quote.POSITION_HISTORY);

            getWindow().getDecorView().setContentDescription(
                    String.format(getString(R.string.detail_activity_cd), stockName));

            DecimalFormat dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.getDefault());
            DecimalFormat dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.getDefault());
            dollarFormatWithPlus.setPositivePrefix("+$");

            tvStockName.setText(stockName);
            tvStockPrice.setText(dollarFormat.format(stockPrice));
            tvStockPrice.setContentDescription(String.format(getString(R.string.stock_price_cd), tvStockPrice.getText()));

            tvStockChange.setText(dollarFormatWithPlus.format(stockChange));
            if (stockChange > 0) {
                tvStockChange.setBackgroundResource(R.drawable.percent_change_pill_green);
                tvStockChange.setContentDescription(
                        String.format(getString(R.string.stock_increment_cd), tvStockChange.getText()));
            } else {
                tvStockChange.setBackgroundResource(R.drawable.percent_change_pill_red);
                tvStockChange.setContentDescription(
                        String.format(getString(R.string.stock_decrement_cd), tvStockChange.getText()));
            }

            if(lowest != -1) {
                tvHighest.setText(dollarFormat.format(highest));
                tvHighest.setContentDescription(String.format(getString(R.string.day_highest_cd), tvHighest.getText()));
                tvHighest.setVisibility(View.VISIBLE);
                tvLowest.setText(dollarFormat.format(lowest));
                tvLowest.setContentDescription(String.format(getString(R.string.day_lowest_cd), tvLowest.getText()));
                tvLowest.setVisibility(View.VISIBLE);
            } else {
                tvHighest.setVisibility(View.GONE);
                tvLowest.setVisibility(View.GONE);
            }

            drawChart(historyData);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void drawChart(String historyData) {
        Pair<Long, List<Entry>> result = Parser.getFormattedStockHistory(historyData);
        List<Entry> dataPairs = result.second;
        Long referenceTime = result.first;
        LineDataSet dataSet = new LineDataSet(dataPairs, "");
        dataSet.setColor(white);
        dataSet.setLineWidth(2f);
        dataSet.setDrawHighlightIndicators(false);
        dataSet.setCircleColor(white);
        dataSet.setHighLightColor(white);
        dataSet.setDrawValues(false);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setEnabled(false);

        YAxis yAxisLeft = lineChart.getAxisLeft();
        yAxisLeft.setEnabled(false);

        YAxis yAxisRight = lineChart.getAxisRight();
        yAxisRight.setEnabled(false);



        Legend legend = lineChart.getLegend();
        legend.setEnabled(false);

        lineChart.setDragEnabled(false);
        lineChart.setScaleEnabled(false);
        lineChart.setDragDecelerationEnabled(false);
        lineChart.setPinchZoom(false);
        lineChart.setDoubleTapToZoomEnabled(false);
        Description description = new Description();
        description.setText(" ");
        lineChart.setDescription(description);
        lineChart.setExtraOffsets(10, 0, 0, 10);
        lineChart.animateX(1500, Easing.EasingOption.Linear);
    }
}
