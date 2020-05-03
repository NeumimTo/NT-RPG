package cz.neumimto.rpg;


import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Created by NeumimTo on 16.9.2018.
 */
@Singleton
public class A {

    @Inject
    B b;

    public void callB() {
        b.b();
    }

    public void callBWhichCallsA() {
        b.a.callB();
    }

    public void a() {
        System.out.println("A.a()");
    }
}
