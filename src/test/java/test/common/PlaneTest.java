package test.common;

import com.builtbroken.artillects.core.region.Plane;
import com.builtbroken.mc.lib.transform.vector.Point;

public class PlaneTest
{
    public static void main(String... args)
    {
        System.out.println("Doing unit test of Plane.class\n");
        System.out.println("==Setup Test==");
        Point start = new Point(-5, -5);
        Point end = new Point(5, 5);
        Plane plane = new Plane(start, end);

        System.out.println("Start: " + start);
        System.out.println("End: " + end);
        System.out.println("Plane: " + plane);
        
        
        System.out.println("\n==Connection Test==");

        Plane centerPlane = new Plane(-1, -1, 1, 1);
        Plane leftPlane = new Plane(-2, -1, -1, 1);
        Plane rightPlane = new Plane(1, -1, 3, 1);
        Plane topPlane = new Plane(-1, 1, 1, 3);
        Plane bottomPlane = new Plane(-1, -3, 1, -1);
        
        Plane plane1 = new Plane(10, 10, 12, 12);
        
        System.out.println("CenterPlane: " + centerPlane);
        System.out.println("LeftPlane: " + leftPlane);
        System.out.println("RightPlane: " + rightPlane);
        System.out.println("TopPlane: " + topPlane);
        System.out.println("BottomPlane: " + bottomPlane);
        
        System.out.println("LeftToCenter: " + centerPlane.isConnected(leftPlane));
        System.out.println("RightToCenter: " + centerPlane.isConnected(rightPlane));
        System.out.println("TopToCenter: " + centerPlane.isConnected(topPlane));
        System.out.println("BottomToCenter: " + centerPlane.isConnected(bottomPlane));
        System.out.println("r1ToCenter: " + centerPlane.isConnected(plane1));

    }
}
