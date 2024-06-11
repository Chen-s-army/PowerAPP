package com.example.demo1.util;

import com.github.abel533.echarts.axis.Axis;
import com.github.abel533.echarts.axis.CategoryAxis;
import com.github.abel533.echarts.axis.ValueAxis;
import com.github.abel533.echarts.code.Trigger;
import com.github.abel533.echarts.json.GsonOption;
import com.github.abel533.echarts.series.Bar;
import com.github.abel533.echarts.series.Line;

import java.util.ArrayList;
import java.util.List;

public class EchartOptionUtil {

    public static GsonOption getLineChartOptions(Object[] xAxis, Object[] yAxis, Object[] yAxis2, Object[] yAxis3, Object[] yAxis4, Object[] yAxis5, Object[] yAxis6) {
        GsonOption option = new GsonOption();

//        // 设置标题
//        option.title().text("能耗情况").x("center");

        // 设置图例
        option.legend().data("2栋", "3栋", "4栋", "7栋", "8栋", "总功率").y("top");

        // 设置提示框触发方式
        option.tooltip().trigger(Trigger.axis);

        // 设置值轴
        ValueAxis valueAxis = new ValueAxis();
        option.yAxis(valueAxis);

        // 设置类目轴
        CategoryAxis categorxAxis = new CategoryAxis();
        categorxAxis.axisLine().onZero(false);
        categorxAxis.boundaryGap(true);
        categorxAxis.data(xAxis);
        option.xAxis(categorxAxis);

        // 设置系列数据
        Bar bar = new Bar();
        Bar bar2 = new Bar();
        Bar bar3 = new Bar();
        Bar bar4 = new Bar();
        Bar bar5 = new Bar();
        Bar bar6 = new Bar();
        bar.name("2栋").data(yAxis).itemStyle().normal();
        bar2.name("3栋").data(yAxis2).itemStyle().normal();
        bar3.name("4栋").data(yAxis3).itemStyle().normal();
        bar4.name("7栋").data(yAxis4).itemStyle().normal();
        bar5.name("8栋").data(yAxis5).itemStyle().normal();
        bar6.name("总功率").data(yAxis6).itemStyle().normal();
        option.series(bar, bar2, bar3, bar4, bar5, bar6);

        return option;
    }
}
