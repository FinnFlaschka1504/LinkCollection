//package com.maxMustermannGeheim.linkcollection.Daten.Videos;
//
//import com.maxMustermannGeheim.linkcollection.Daten.ParentClass;
//
//import java.util.HashMap;
//import java.util.Map;
//
//public interface Test  {
//    Container<Integer> test = new Container<>();
//
//    public static Test create(){
//        return new Test() {
//            @Override
//            public void setTest(int test) {
//                Test.super.setTest(test);
//            }
//
//            @Override
//            public Integer getTest() {
//                return Test.super.getTest();
//            }
//        };
//    }
//
//    default public void setTest(int test){
//        Test.test.setPayload(this, test);
//    }
//
//    default public Integer getTest() {
//        return test.getPayload(this);
//    }
//}
//
