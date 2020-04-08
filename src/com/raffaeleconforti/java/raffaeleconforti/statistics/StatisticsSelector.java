/*
 *  Copyright (C) 2018 Raffaele Conforti (www.raffaeleconforti.com)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.raffaeleconforti.java.raffaeleconforti.statistics;

import com.raffaeleconforti.java.raffaeleconforti.statistics.max.Max;
import com.raffaeleconforti.java.raffaeleconforti.statistics.mean.Mean;
import com.raffaeleconforti.java.raffaeleconforti.statistics.median.Median;
import com.raffaeleconforti.java.raffaeleconforti.statistics.medianabsolutedeviation.*;
import com.raffaeleconforti.java.raffaeleconforti.statistics.min.Min;
import com.raffaeleconforti.java.raffaeleconforti.statistics.mode.Mode;
import com.raffaeleconforti.java.raffaeleconforti.statistics.modeabsolutedeviation.*;
import com.raffaeleconforti.java.raffaeleconforti.statistics.percentile.Percentile;
import com.raffaeleconforti.java.raffaeleconforti.statistics.qn.*;
import com.raffaeleconforti.java.raffaeleconforti.statistics.sn.*;
import com.raffaeleconforti.java.raffaeleconforti.statistics.standarddeviation.*;


/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 23/11/16.
 */
public class StatisticsSelector {

    public enum StatisticsMeasures {MIN, MAX, MEAN, MEDIAN, MODE, PERCENTILE, SD, LEFT_SD, RIGHT_SD, MAD, LEFT_MAD, RIGHT_MAD, MoAD, LEFT_MoAD, RIGHT_MoAD, SN, LEFT_SN, RIGHT_SN, QN, LEFT_QN, RIGHT_QN}

    private final Min min = new Min();
    private final Max max = new Max();

    private final Mean mean = new Mean();
    private final Median median = new Median();
    private final Mode mode = new Mode();
    private final Percentile percentile = new Percentile();

    private final StandardDeviation sd = new StandardDeviation();
    private final LeftStandardDeviation lsd = new LeftStandardDeviation();
    private final RightStandardDeviation rsd = new RightStandardDeviation();

    private final MedianAbsoluteDeviation mad = new MedianAbsoluteDeviation();
    private final LeftMedianAbsoluteDeviation lmad = new LeftMedianAbsoluteDeviation();
    private final RightMedianAbsoluteDeviation rmad = new RightMedianAbsoluteDeviation();

    private final ModeAbsoluteDeviation moad = new ModeAbsoluteDeviation();
    private final LeftModeAbsoluteDeviation lmoad = new LeftModeAbsoluteDeviation();
    private final RightModeAbsoluteDeviation rmoad = new RightModeAbsoluteDeviation();

    private final Sn sn = new Sn();
    private final LeftSn lsn = new LeftSn();
    private final RightSn rsn = new RightSn();

    private final Qn qn = new Qn();
    private final LeftQn lqn = new LeftQn();
    private final RightQn rqn = new RightQn();

    public double evaluate(StatisticsMeasures measure, Double val, double... values) {
        return retrieveMeasure(measure).evaluate(val, values);
    }

    public double evaluate(StatisticsMeasures measure, Float val, float... values) {
        return retrieveMeasure(measure).evaluate(val, values);
    }

    public double evaluate(StatisticsMeasures measure, Long val, long... values) {
        return retrieveMeasure(measure).evaluate(val, values);
    }

    public double evaluate(StatisticsMeasures measure, Integer val, int... values) {
        return retrieveMeasure(measure).evaluate(val, values);
    }

    private StatisticsMeasure retrieveMeasure(StatisticsMeasures measure) {
        StatisticsMeasure statisticsMeasure = null;
        switch (measure) {
            case MIN       : statisticsMeasure = min;
                break;

            case MAX     : statisticsMeasure = max;
                break;

            case MEAN       : statisticsMeasure = mean;
                break;

            case MEDIAN     : statisticsMeasure = median;
                break;

            case MODE       : statisticsMeasure = mode;
                break;

            case PERCENTILE : statisticsMeasure = percentile;
                break;

            case SD         : statisticsMeasure = sd;
                break;

            case LEFT_SD    : statisticsMeasure = lsd;
                break;

            case RIGHT_SD   : statisticsMeasure = rsd;
                break;

            case MAD        : statisticsMeasure = mad;
                break;

            case LEFT_MAD   : statisticsMeasure = lmad;
                break;

            case RIGHT_MAD  : statisticsMeasure = rmad;
                break;

            case MoAD        : statisticsMeasure = moad;
                break;

            case LEFT_MoAD   : statisticsMeasure = lmoad;
                break;

            case RIGHT_MoAD  : statisticsMeasure = rmoad;
                break;

            case SN         : statisticsMeasure = sn;
                break;

            case LEFT_SN    : statisticsMeasure = lsn;
                break;

            case RIGHT_SN   : statisticsMeasure = rsn;
                break;

            case QN         : statisticsMeasure = qn;
                break;

            case LEFT_QN    : statisticsMeasure = lqn;
                break;

            case RIGHT_QN   : statisticsMeasure = rqn;
                break;
        }

        return statisticsMeasure;
    }
}
