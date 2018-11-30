package com.pa.chen.apprxandroid;

import android.util.Log;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;

public class ModelImpl {

    //数据源返回被观察者,发布者
    public Observable<String> createObservable1() {
        return Observable.create(new ObservableOnSubscribe<String>() {//数据发射
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                Log.d(TAG.RXAndroid_1, "begin emitter !");
                Log.d(TAG.RXAndroid_1, "emitter rxandroid-----one !");
                e.onNext("rxandroid-----one");
                Log.d(TAG.RXAndroid_1, "emitter rxandroid-----two !");
                e.onNext("rxandroid-----two");
                Log.d(TAG.RXAndroid_1, "emitter rxandroid-----three !");
                e.onNext("rxandroid-----three");
                //e.onComplete();
                Log.d(TAG.RXAndroid_1, "emitter rxandroid-----four !");
                e.onNext("rxandroid-----four");
            }
        });
    }

    //数据源返回被观察者,发布者
    public Observable<Integer> createObservable2() {
        return Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                Log.d(TAG.RXAndroid_2, "Observable : " + Thread.currentThread().getName());
                e.onNext(2);
                e.onComplete();
            }
        });
    }

    //数据源返回被观察者
    public Observable<RxResponse> createObservable3() {
        return Observable.just("code_24", "code_25")
                .map(new Function<String, String>() {
                    @Override
                    public String apply(String s) throws Exception {
                        return s + "_gc";
                    }
                })
                .map(new Function<String, RxMediaValue>() {
                    @Override
                    public RxMediaValue apply(String code) throws Exception {
                        Log.d(TAG.RXAndroid_3, "String apply RxMediaValue " + code);
                        return  new RxMediaValue(code + "_m");
                    }
                })
                .map(new Function<RxMediaValue, RxResponse>() {
                    @Override
                    public RxResponse apply(RxMediaValue rxMediaValue) throws Exception {
                        Log.d(TAG.RXAndroid_3, "RxMediaValue apply RxResponse  " + rxMediaValue.getCode());
                        return new RxResponse(rxMediaValue.getCode() + "_resp");
                    }
                });

    }

    //flatMap操作符 flatMap并不保证事件的顺序,concatMap有序的
    //在flatMap中将上游发来的每个事件转换
    public Observable<RxResponse> createObservable4() {
        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                Log.d(TAG.RXAndroid_4, "s emitter " + "code_26");//发射
                emitter.onNext("code_26");
                emitter.onComplete();
            }
        }).map(new Function<String, String>() {
            @Override
            public String apply(String s) throws Exception {
                String newStr = s + "_gc";
                Log.d(TAG.RXAndroid_4, "s apply s " + newStr);
                return newStr;
            }
        })
                .flatMap(new Function<String, ObservableSource<RxMediaValue>>() {
                    @Override
                    public ObservableSource<RxMediaValue> apply(String s) throws Exception {
                        RxRequest request = new RxRequest(s + "_req");
                        Log.d(TAG.RXAndroid_4, "s apply ObservableSource<RxMediaValue> " + request.getCode());
                        return Observable.just(request).flatMap(new Function<RxRequest, ObservableSource<RxMediaValue>>() {
                            @Override
                            public ObservableSource<RxMediaValue> apply(RxRequest rxRequest) throws Exception {
                                RxMediaValue rxMediaValue = new RxMediaValue(rxRequest.getCode() + "_m");
                                Log.d(TAG.RXAndroid_4, "rxRequest apply ObservableSource<RxMediaValue> " + rxMediaValue.getCode());
                                return Observable.just(rxMediaValue);
                            }
                        });
                    }
                })
                .flatMap(new Function<RxMediaValue, ObservableSource<RxResponse>>() {
                    @Override
                    public ObservableSource<RxResponse> apply(RxMediaValue rxMediaValue) throws Exception {
                        RxResponse rxResponse = new RxResponse(rxMediaValue.getCode() + "_resp");
                        Log.d(TAG.RXAndroid_4, "rxMediaValue apply ObservableSource<RxResponse> " + rxResponse.getCode());
                        return Observable.just(rxResponse);
                    }
                });
    }

    //数据源返回被观察者,发布者
    public Observable<RxResponse> createObservable5() {
        return Observable.create(new ObservableOnSubscribe<RxResponse>() {
            @Override
            public void subscribe(ObservableEmitter<RxResponse> emitter) throws Exception {
                RxResponse rxResponse = new RxResponse("code_201");
                Log.d(TAG.RXAndroid_5, "201 Observable : " + Thread.currentThread().getName());
                emitter.onNext(rxResponse);
            }
        });
    }


}
