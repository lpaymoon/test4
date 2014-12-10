package web;

import java.lang.reflect.Method;

public class TestLogInterceptor {

	public static void main(String[] args){
        // Get the Class object associated with the class String.
        Class strClass = String.class;

        // Get all methods for class String.
        Method[] methods = strClass.getMethods();
        for (int i = 0; i < methods.length; i++)
        {
            Class declaring = methods[i].getDeclaringClass();
            System.out.println("Method: " + methods[i].toString() +
                " Declaring class: " + declaring.toString());
        }

	}
}
