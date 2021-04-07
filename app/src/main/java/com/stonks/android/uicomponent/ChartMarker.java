package com.stonks.android.uicomponent;

import android.content.Context;
import android.graphics.Canvas;
import android.widget.TextView;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.stonks.android.R;
import java.util.function.Function;

public class ChartMarker extends MarkerView {
    private final TextView markerText;
    private final Function<Integer, String> getMarkerText;

    public ChartMarker(
            Context context, int layoutResource, Function<Integer, String> getMarkerText) {
        super(context, layoutResource);

        this.markerText = findViewById(R.id.marker_text_view);
        this.getMarkerText = getMarkerText;
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        markerText.setText(this.getMarkerText.apply((int) e.getX()));

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
