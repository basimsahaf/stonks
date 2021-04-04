package com.stonks.android.uicomponent;

import android.content.Context;
import android.graphics.Canvas;
import android.widget.TextView;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.stonks.android.R;

public class ChartMarker extends MarkerView {
    private final TextView markerText;

    public ChartMarker(Context context, int layoutResource) {
        super(context, layoutResource);

        markerText = findViewById(R.id.marker_text_view);
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        CandleEntry entry = (CandleEntry) e;

        markerText.setText(entry.getOpen() + " - " + entry.getClose());

        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() / 2f), getChartView().getYMax());
    }

    @Override
    public void draw(Canvas canvas, float posX, float posY) {
        MPPointF offset = getOffsetForDrawingAtPoint(posX, posY);

        int saveId = canvas.save();
        // translate to the correct position and draw
        canvas.translate(posX + offset.x, 0);
        draw(canvas);
        canvas.restoreToCount(saveId);
    }
}
