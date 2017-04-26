package com.aol.cyclops.rx.hkt.typeclasses.instances;
import static com.aol.cyclops.rx.hkt.ObservableKind.widen;
import static com.aol.cyclops.util.function.Lambda.l1;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.function.Function;

import com.aol.cyclops.rx.hkt.ObservableKind;
import org.junit.Test;

import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.hkt.alias.Higher;
import com.aol.cyclops.hkt.cyclops.MaybeType;
import com.aol.cyclops.hkt.instances.cyclops.MaybeInstances;
import com.aol.cyclops.rx.Observables;
import com.aol.cyclops.rx.hkt.typeclassess.instances.ObservableInstances;
import com.aol.cyclops.util.function.Lambda;

import rx.Observable;

public class ObservableTest {

    @Test
    public void unit(){
        
        ObservableKind<String> list = ObservableInstances.unit()
                                     .unit("hello")
                                     .convert(ObservableKind::narrowK);
        
        assertThat(Observables.reactiveSeq(list.narrow()).toList(),equalTo(Arrays.asList("hello")));
    }
    @Test
    public void functor(){
        
        ObservableKind<Integer> list = ObservableInstances.unit()
                                     .unit("hello")
                                     .then(h->ObservableInstances.functor().map((String v) ->v.length(), h))
                                     .convert(ObservableKind::narrowK);
        
        assertThat(Observables.reactiveSeq(list.narrow()).toList(),equalTo(Arrays.asList("hello".length())));
    }
    @Test
    public void apSimple(){
        ObservableInstances.zippingApplicative()
            .ap(widen(Observable.just(l1(this::multiplyByTwo))),widen(Observable.just(1,2,3)));
    }
    private int multiplyByTwo(int x){
        return x*2;
    }
    @Test
    public void applicative(){
        
        ObservableKind<Function<Integer,Integer>> listFn =ObservableInstances.unit().unit(Lambda.l1((Integer i) ->i*2)).convert(ObservableKind::narrowK);
        
        ObservableKind<Integer> list = ObservableInstances.unit()
                                     .unit("hello")
                                     .then(h->ObservableInstances.functor().map((String v) ->v.length(), h))
                                     .then(h->ObservableInstances.zippingApplicative().ap(listFn, h))
                                     .convert(ObservableKind::narrowK);
        
        assertThat(Observables.reactiveSeq(list.narrow()).toList(),equalTo(Arrays.asList("hello".length()*2)));
    }
    @Test
    public void monadSimple(){
       ObservableKind<Integer> list  = ObservableInstances.monad()
                                      .flatMap(i->widen(ReactiveSeq.range(0,i)), widen(Observable.just(1,2,3)))
                                      .convert(ObservableKind::narrowK);
    }
    @Test
    public void monad(){
        
        ObservableKind<Integer> list = ObservableInstances.unit()
                                     .unit("hello")
                                     .then(h->ObservableInstances.monad().flatMap((String v) ->ObservableInstances.unit().unit(v.length()), h))
                                     .convert(ObservableKind::narrowK);
        
        assertThat(Observables.reactiveSeq(list.narrow()).toList(),equalTo(Arrays.asList("hello".length())));
    }
    @Test
    public void monadZeroFilter(){
        
        ObservableKind<String> list = ObservableInstances.unit()
                                     .unit("hello")
                                     .then(h->ObservableInstances.monadZero().filter((String t)->t.startsWith("he"), h))
                                     .convert(ObservableKind::narrowK);
        
        assertThat(Observables.reactiveSeq(list.narrow()).toList(),equalTo(Arrays.asList("hello")));
    }
    @Test
    public void monadZeroFilterOut(){
        
        ObservableKind<String> list = ObservableInstances.unit()
                                     .unit("hello")
                                     .then(h->ObservableInstances.monadZero().filter((String t)->!t.startsWith("he"), h))
                                     .convert(ObservableKind::narrowK);
        
        assertThat(Observables.reactiveSeq(list.narrow()).toList(),equalTo(Arrays.asList()));
    }
    
    @Test
    public void monadPlus(){
        ObservableKind<Integer> list = ObservableInstances.<Integer>monadPlus()
                                      .plus(ObservableKind.widen(Observable.empty()), ObservableKind.widen(Observable.just(10)))
                                      .convert(ObservableKind::narrowK);
        assertThat(Observables.reactiveSeq(list.narrow()).toList(),equalTo(Arrays.asList(10)));
    }
/**
    @Test
    public void monadPlusNonEmpty(){
        
        Monoid<ObservableKind<Integer>> m = Monoid.of(ObservableKind.widen(Observable.empty()), (a,b)->a.isEmpty() ? b : a);
        ObservableKind<Integer> list = ObservableInstances.<Integer>monadPlus(m)
                                      .plus(ObservableKind.widen(Observable.of(5)), ObservableKind.widen(Observable.of(10)))
                                      .convert(ObservableKind::narrowK);
        assertThat(list,equalTo(Arrays.asList(5)));
    }
**/
    @Test
    public void  foldLeft(){
        int sum  = ObservableInstances.foldable()
                        .foldLeft(0, (a,b)->a+b, ObservableKind.widen(Observable.just(1,2,3,4)));
        
        assertThat(sum,equalTo(10));
    }
    @Test
    public void  foldRight(){
        int sum  = ObservableInstances.foldable()
                        .foldRight(0, (a,b)->a+b, ObservableKind.widen(Observable.just(1,2,3,4)));
        
        assertThat(sum,equalTo(10));
    }
    @Test
    public void traverse(){
       MaybeType<Higher<ObservableKind.µ, Integer>> res = ObservableInstances.traverse()
                                                         .traverseA(MaybeInstances.applicative(), (Integer a)->MaybeType.just(a*2), ObservableKind.just(1,2,3))
                                                         .convert(MaybeType::narrowK);
       
       
       assertThat(res.map(i->Observables.reactiveSeq(ObservableKind.narrow(i)).toList()),
                  equalTo(Maybe.just(ListX.of(2,4,6))));
    }
   
    
}
