package main.model;

public enum Grade {
     A1(4.00, true, true),
     A2(3.60, true, true),
     B1(3.20, true, true),
     B2(3.00, true, true),
     B3(2.80, true, true),
     C1(2.60, true, true),
     C2(2.40, true, true),
     C3(2.00, true, true),
     D1(1.60, true, true),     //compensating fail 
     F(0.00, false, true),
     NG(0.00, false, true),
     P(0.00, true, false),    // pass on a pass/fail module: no QPV 
     N(0.00, false, false);   // fail on a pass/ fail module: no QPV 

     private final double qcaPoints;
     private final boolean creditsAwarded;
     private final boolean countsInQca;

     Grade(double qcaPoints, boolean creditsAwarded, boolean countsInQca){
        this.qcaPoints = qcaPoints;
        this.creditsAwarded = creditsAwarded;
        this.countsInQca = countsInQca;
     }
     
     public double getQcaPoints() { return qcaPoints;}
     public boolean isCreditsAwarded() { return creditsAwarded;}
     public boolean countsTowardsQca() { return countsInQca;}

     /** Pass = C3 or better (40%+), or P. D1 is a compensating fail, not a pass. */

     public boolean isPass() {
        return this == P || ( countsInQca && qcaPoints >= C3.qcaPoints);

     }

     /** D1 is a fail that may be compensated.  */
     public boolean isCompensatingFail(){
        return this == D1;
     }

}
