package com.pa.chen.apprxandroid;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/*
 * RxAndroid
 */
public class RxActivity extends Activity {
    @BindView(R.id.tv_rxandroid)
    TextView mTvRxAndroid;

    ModelImpl mModelImpl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_rx_android);
        ButterKnife.bind(this);
        mModelImpl = new ModelImpl();
    }

    @OnClick({R.id.btn_rxandroid1, R.id.btn_rxandroid2, R.id.btn_rxandroid3, R.id.btn_rxandroid4, R.id.btn_rxandroid5,
            R.id.btn_rxandroid6, R.id.btn_rxandroid7})
    void OnRxClick(View view) {
        switch (view.getId()) {
            case R.id.btn_rxandroid1://基本被观察者与订阅者
                mModelImpl.createObservable1().subscribe(new Observer<String>() {
                    Disposable disposable;
                    int index = 0;

                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.d(AppLog.RXAndroid_1, "Observer: " + Thread.currentThread().getName());
                        disposable = d;
                    }

                    @Override
                    public void onNext(String s) {
                        Log.d(AppLog.RXAndroid_1, s);
                        index++;
//                        if (index == 2) {
//                            disposable.dispose();//截断后，next与complete都收不到
//                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(AppLog.RXAndroid_1, "onError");//onError后，onNext收不到,结束
                    }

                    @Override
                    public void onComplete() {
                        Log.d(AppLog.RXAndroid_1, "onComplete");//complete后，next收不到,结束
                        mTvRxAndroid.setText("rxandroid");
                    }
                });
                break;
            case R.id.btn_rxandroid2://线程
                mModelImpl.createObservable2()//发布者在子线程
                        .subscribeOn(Schedulers.newThread())
                        .doOnNext(new Consumer<Integer>() {//第一个观察者在子线程
                            @Override
                            public void accept(Integer integer) throws Exception {
                                Log.d(AppLog.RXAndroid_2, "Observer : " + Thread.currentThread().getName());
                                Log.d(AppLog.RXAndroid_2, "accept:doOnNext:" + integer);
                            }
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<Integer>() {//第二个在主线程
                            @Override
                            public void accept(Integer integer) throws Exception {
                                Log.d(AppLog.RXAndroid_2, "Observer : " + Thread.currentThread().getName());
                                Log.d(AppLog.RXAndroid_2, "accept:" + integer);
                            }
                        });
                break;
            case R.id.btn_rxandroid3://发布者 map
                Observable<RxResponse> mObservable = new ModelImpl().createObservable3();
                mObservable.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())//观察者在主线程
                        .subscribe(new Consumer<RxResponse>() {
                            @Override
                            public void accept(RxResponse rxResponse) throws Exception {
                                Log.d(AppLog.RXAndroid_3, " Observer " + Thread.currentThread().getName() + "  code: " + rxResponse.getCode());
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                Log.d(AppLog.RXAndroid_3, " Observer " + Thread.currentThread().getName() + " " + throwable.toString());
                            }
                        });
                break;
            case R.id.btn_rxandroid4://操作符flatMap
                mModelImpl.createObservable4().subscribe(new Consumer<RxResponse>() {
                    @Override
                    public void accept(RxResponse response) throws Exception {
                        Log.d(AppLog.RXAndroid_4, "Observer : " + response.getCode());
                    }
                });
                break;
            case R.id.btn_rxandroid5://两个连接
                mModelImpl.createObservable5().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                        .doOnNext(new Consumer<RxResponse>() {
                            @Override
                            public void accept(RxResponse rxResponse) throws Exception {
                                Log.d(AppLog.RXAndroid_5, "201 Observer  : " + Thread.currentThread().getName() + " " + rxResponse.getCode());
                            }
                        }).observeOn(Schedulers.io()) //回到IO线程去进行第二次发射
                        .flatMap(new Function<RxResponse, ObservableSource<RxResponse>>() {
                            @Override
                            public ObservableSource<RxResponse> apply(RxResponse rxResponse) throws Exception {
                                Log.d(AppLog.RXAndroid_5, "201 apply 202  : " + Thread.currentThread().getName());
                                return Observable.just(new RxResponse(rxResponse.getCode() + "_202"));
                            }
                        })
                        .observeOn(AndroidSchedulers.mainThread())//回到主线程去处理第二次发射
                        .subscribe(new Consumer<RxResponse>() {
                            @Override
                            public void accept(RxResponse rxResponse) throws Exception {
                                Log.d(AppLog.RXAndroid_5, "202 Observer  : " + Thread.currentThread().getName() + " " + rxResponse.getCode());
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                Log.d(AppLog.RXAndroid_5, throwable.getMessage());
                            }

                        });
                break;
            case R.id.btn_rxandroid6://zip合并
                Observable mObservable1 = Observable.create(new ObservableOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                        Log.d(AppLog.RXAndroid_6, "mObservable1 emit 1" + Thread.currentThread().getName());
                        e.onNext(6);
                        Log.d(AppLog.RXAndroid_6, "mObservable1 emit 2" + Thread.currentThread().getName());
                        e.onNext(7);
                        Log.d(AppLog.RXAndroid_6, "mObservable1 emit complete1" + Thread.currentThread().getName());
                        e.onComplete();
                    }
                }).subscribeOn(Schedulers.io());//两个水管运行在不同线程
                Observable mObservable2 = Observable.create(new ObservableOnSubscribe<String>() {
                    @Override
                    public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                        Log.d(AppLog.RXAndroid_6, "mObservable2 emit :A_" + Thread.currentThread().getName());
                        emitter.onNext(":A_");
                        try {
                            Thread.sleep(10000);//做一个延迟，让线程2等一会再发送。这时，线程1已经发送完，等线程2发射B，合并
                        } catch (Exception e) {
                        }
                        Log.d(AppLog.RXAndroid_6, "mObservable2 emit :B_" + Thread.currentThread().getName());
                        emitter.onNext(":B_");
                        Log.d(AppLog.RXAndroid_6, "mObservable2 emit complete2" + Thread.currentThread().getName());
                        emitter.onComplete();
                    }
                }).subscribeOn(Schedulers.io());
                //两根水管若运行在同一个线程里, 同一个线程里执行代码肯定有先后顺序
                Observable.zip(mObservable1, mObservable2, new BiFunction<Integer, String, String>() {
                    @Override
                    public String apply(Integer integer, String s) throws Exception {
                        Log.d(AppLog.RXAndroid_6, "zip apply:" + Thread.currentThread().getName());
                        return integer + s;
                    }
                }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String zip) throws Exception {
                        Log.d(AppLog.RXAndroid_6, "Observer : " + zip + Thread.currentThread().getName());
                    }
                });
                break;

            case R.id.btn_rxandroid7:
                Observable.create(new ObservableOnSubscribe<String>() {
                    @Override
                    public void subscribe(ObservableEmitter<String> e) throws Exception {
                        Log.d(AppLog.RXAndroid_7, Thread.currentThread().getName() + "-0");

                        e.onNext("fileName");
                    }
                }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).map(new Function<String, File>() {
                    @Override
                    public File apply(String s) throws Exception {
                        Log.d(AppLog.RXAndroid_7, Thread.currentThread().getName() + "-1");
                        File file = new File(s);
                        return file;
                    }
                }).subscribe(new Consumer<File>() {
                    @Override
                    public void accept(File file) throws Exception {
                        Log.d(AppLog.RXAndroid_7, Thread.currentThread().getName() + "-3");
                        Log.d(AppLog.RXAndroid_7, file.getAbsolutePath());
                    }
                });

                break;
            default:
                break;

        }
    }
}
