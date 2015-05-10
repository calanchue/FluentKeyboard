package kr.ac.kaist.jinhwan.fluentkeyboard;


import android.util.Log;

public enum Direction {
    E(360*0/8f),
    NE(360*1/8f),
    N(360*2/8f),
    NW(360*3/8f),
    W(360*4/8f),
    SW(360*5/8f),
    S(360*6/8f),
    SE(360*7/8f),
    NON(null);

    Float angle;
    Direction(Float angle){
        this.angle = angle;
    }

    /**
     * Returns a direction given an angle.
     * Directions are defined as follows:
     * <p/>
     * Up: [45, 135]
     * Right: [0,45] and [315, 360]
     * Down: [225, 315]
     * Left: [135, 225]
     *
     * @param angle an angle from 0 to 360 - e
     * @return the direction of an angle
     */
    public static Direction get(double angle) {
        if (inRange(angle, 0, 360*1/16) || inRange(angle, 360*15/16, 360)) {
            return Direction.E;
        }else if (inRange(angle, 360*1/16, 360*3/16) ) {
            return Direction.NE;
        }else if (inRange(angle, 360*3/16, 360*5/16) ) {
            return Direction.N;
        }else if (inRange(angle, 360*5/16, 360*7/16) ) {
            return Direction.NW;
        }else if (inRange(angle, 360*7/16, 360*9/16) ) {
            return Direction.W;
        }else if (inRange(angle, 360*9/16, 360*11/16) ) {
            return Direction.SW;
        }else if (inRange(angle, 360*11/16, 360*13/16) ) {
            return Direction.S;
        }else if (inRange(angle, 360*13/16, 360*15/16) ) {
            return Direction.SE;
        }else{
            return null;
        }
/*
            if (inRange(angle, 45, 135)) {
                return Direction.N;
            } else if (inRange(angle, 0, 360*1/16) || inRange(angle, 360*15/16, 360)) {
                return Direction.E;
            } else if (inRange(angle, 225, 315)) {
                return Direction.S;
            } else {
                return Direction.W;
            }
*/
    }
    public static Direction get4(double angle) {
        if (inRange(angle, 45, 135)) {
            return Direction.N;
        } else if (inRange(angle, 0, 360*1/16) || inRange(angle, 360*15/16, 360)) {
            return Direction.E;
        } else if (inRange(angle, 225, 315)) {
            return Direction.S;
        } else {
            return Direction.W;
        }
    }


    /**
     * @param angle an angle
     * @param init  the initial bound
     * @param end   the final bound
     * @return returns true if the given angle is in the interval [init, end).
     */
    private static boolean inRange(double angle, float init, float end) {
        if(angle < 0 ){
            angle = 360+angle;
        }

        if(init < 0 ){
            init = 360 + init;
        }

        if(end < 0 ){
            end = 360 + end;
        }

        return (angle >= init) && (angle < end);
    }

    /**
     * Given two points in the plane p1=(x1, x2) and p2=(y1, y1), this method
     * returns the direction that an arrow pointing from p1 to p2 would have.
     * @param x1 the x position of the first point
     * @param y1 the y position of the first point
     * @param x2 the x position of the second point
     * @param y2 the y position of the second point
     * @return the direction
     */
    public static Direction getDirection(float x1, float y1, float x2, float y2){
        double angle = getAngle(x1, y1, x2, y2);
        return Direction.get(angle);
    }

    public static Direction getDirection4(float x1, float y1, float x2, float y2){
        double angle = getAngle(x1, y1, x2, y2);
        return Direction.get4(angle);
    }

    /**
     *
     * Finds the angle between two points in the plane (x1,y1) and (x2, y2)
     * The angle is measured with 0/360 being the X-axis to the right, angles
     * increase counter clockwise.
     *
     * @param x1 the x position of the first point
     * @param y1 the y position of the first point
     * @param x2 the x position of the second point
     * @param y2 the y position of the second point
     * @return the angle between two points
     */
    public static double getAngle(float x1, float y1, float x2, float y2) {
        double rad = Math.atan2(y1-y2,x2-x1) + Math.PI;
        return (rad*180/Math.PI + 180)%360;
    }

    public static Direction getOpposite(Direction dir){
        return Direction.values()[(dir.ordinal()+4) %8];
    }

    //this tanslate GUI coord to  normal coord
    public static boolean sameField(float x1, float y1, float x2, float y2, Direction dir){
        double vx =Math.cos(dir.angle/360*(2*Math.PI));
        double vy =Math.sin(dir.angle/360*(2*Math.PI));

        double ix = x2-x1;
        double iy = -(y2-y1);

        double vxix = vx*ix;
        double vyiy = vy*iy;
        //Log.v("bent2", String.format("1(%f,%f), 2(%f,%f)", x1,y1, x2,y2));
        //Log.v("bent2", String.format("ixy(%f,%f) vx*ix(%f) vy*iy(%f)", ix,iy, vxix, vyiy));

        double dotProduct = vx*ix + vy*iy;
        //Log.v("bent2", String.format("same field = dot(%f), (%f,%f) to (%f,%f), dir(%f, %f)", dotProduct, x1,y1, x2,y2, vx,vy));

        return dotProduct > 0;
    }
}