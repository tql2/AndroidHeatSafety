package erg.com.nioshheatindex;

class calculateHeatIndex
{
    public static double heatIndexCal(double F, int rh)
    {
        double calculatedHeatIndexF;

        double simpleCalculation = 0.5 * (F + 61.0 + ((F - 68.0) * 1.2) + (rh * 0.094));
        double simpleCalculationAverage = (simpleCalculation + F) / 2;

        calculatedHeatIndexF = simpleCalculationAverage;

        if (simpleCalculationAverage > 80.0)
        {
            calculatedHeatIndexF = -42.379 + (2.04901523 * F);
            calculatedHeatIndexF += 10.14333127 * rh;
            calculatedHeatIndexF -= 0.22475541 * F * rh;
            calculatedHeatIndexF -= 6.83783 * Math.pow(10,-3) * Math.pow(F,2);
            calculatedHeatIndexF -= 5.481717 * Math.pow(10,-2) * Math.pow(rh,2);
            calculatedHeatIndexF += 1.22874 * Math.pow(10,-3) * Math.pow(F,2) * rh;
            calculatedHeatIndexF += 8.5282 * Math.pow(10,-4) * F * Math.pow(rh,2);
            calculatedHeatIndexF -= 1.99 * Math.pow(10,-6) *  Math.pow(F,2) * Math.pow(rh,2);
        }

        if(rh < 13.0 && (F >= 80.0 && F <= 112.0))
        {
            calculatedHeatIndexF -= ((13 - rh) / 4) * Math.sqrt((17 - Math.abs(F - 95.0)) / 17);
        }

        if(rh > 85.0 && (F >= 80.0 && F <= 87))
        {
            calculatedHeatIndexF += ((rh - 85) / 10) * ((87 - F) / 5);
        }

        return Math.round(calculatedHeatIndexF);
    }
}
