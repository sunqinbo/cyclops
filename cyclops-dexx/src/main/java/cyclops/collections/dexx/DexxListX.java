package cyclops.collections.dexx;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import com.aol.cyclops2.data.collections.extensions.CollectionX;
import com.aol.cyclops2.data.collections.extensions.lazy.immutable.LazyLinkedListX;
import com.aol.cyclops2.types.Unwrapable;
import cyclops.collections.immutable.LinkedListX;
import cyclops.function.Reducer;
import cyclops.stream.ReactiveSeq;
import org.jooq.lambda.tuple.Tuple2;
import org.pcollections.PStack;



import com.github.andrewoma.dexx.collection.Builder;
import com.github.andrewoma.dexx.collection.ConsList;
import com.github.andrewoma.dexx.collection.List;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import lombok.experimental.Wither;



@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DexxListX<T> extends AbstractList<T>implements PStack<T>, Unwrapable {

    @Override
    public <R> R unwrap() {
        return (R)list;
    }

    public static <T> LinkedListX<T> copyFromCollection(CollectionX<T> vec) {

        return PStack(from(vec.iterator(),0));

    }
    private static <E> List<E> from(final Iterator<E> i, int depth) {

        if(!i.hasNext())
            return ConsList.empty();
        E e = i.next();
        return  from(i,depth++).prepend(e);
    }
    /**
     * Create a LazyLinkedListX from a Stream
     * 
     * @param stream to construct a LazyQueueX from
     * @return LazyLinkedListX
     */
    public static <T> LazyLinkedListX<T> fromStream(Stream<T> stream) {
        Reducer<PStack<T>> r = toPStack();
        return new LazyLinkedListX<T>(null, ReactiveSeq.fromStream(stream),  r);
    }

    /**
     * Create a LazyLinkedListX that contains the Integers between start and end
     * 
     * @param start
     *            Number of range to start from
     * @param end
     *            Number for range to end at
     * @return Range ListX
     */
    public static LazyLinkedListX<Integer> range(int start, int end) {
        return fromStream(ReactiveSeq.range(start, end));
    }

    /**
     * Create a LazyLinkedListX that contains the Longs between start and end
     * 
     * @param start
     *            Number of range to start from
     * @param end
     *            Number for range to end at
     * @return Range ListX
     */
    public static LazyLinkedListX<Long> rangeLong(long start, long end) {
        return fromStream(ReactiveSeq.rangeLong(start, end));
    }

    /**
     * Unfold a function into a ListX
     * 
     * <pre>
     * {@code 
     *  LazyLinkedListX.unfold(1,i->i<=6 ? Optional.of(Tuple.tuple(i,i+1)) : Optional.empty());
     * 
     * //(1,2,3,4,5)
     * 
     * }</pre>
     * 
     * @param seed Initial value 
     * @param unfolder Iteratively applied function, terminated by an empty Optional
     * @return ListX generated by unfolder function
     */
    public static <U, T> LazyLinkedListX<T> unfold(U seed, Function<? super U, Optional<Tuple2<T, U>>> unfolder) {
        return fromStream(ReactiveSeq.unfold(seed, unfolder));
    }

    /**
     * Generate a LazyLinkedListX from the provided Supplier up to the provided limit number of times
     * 
     * @param limit Max number of elements to generate
     * @param s Supplier to generate ListX elements
     * @return ListX generated from the provided Supplier
     */
    public static <T> LazyLinkedListX<T> generate(long limit, Supplier<T> s) {

        return fromStream(ReactiveSeq.generate(s)
                                     .limit(limit));
    }

    /**
     * Create a LazyLinkedListX by iterative application of a function to an initial element up to the supplied limit number of times
     * 
     * @param limit Max number of elements to generate
     * @param seed Initial element
     * @param f Iteratively applied to each element to generate the next element
     * @return ListX generated by iterative application
     */
    public static <T> LazyLinkedListX<T> iterate(long limit, final T seed, final UnaryOperator<T> f) {
        return fromStream(ReactiveSeq.iterate(seed, f)
                                     .limit(limit));
    }

    /**
     * <pre>
     * {@code 
     * PStack<Integer> q = JSPStack.<Integer>toPStack()
                                     .mapReduce(Stream.of(1,2,3,4));
     * 
     * }
     * </pre>
     * @return Reducer for PStack
     */
    public static <T> Reducer<PStack<T>> toPStack() {
        return Reducer.<PStack<T>> of(DexxListX.emptyPStack(), (final PStack<T> a) -> b -> a.plusAll(b),
                                      (final T x) -> DexxListX.singleton(x));
    }

    public static <T> DexxListX<T> fromList(List<T> list) {
        return new DexxListX<>(
                                 list);
    }

    public static <T> DexxListX<T> emptyPStack() {

        return new DexxListX<>(
                                ConsList.empty());
    }

    public static <T> LazyLinkedListX<T> empty() {
        return fromPStack(new DexxListX<>(ConsList.empty()),
                                      toPStack());
    }

    public static <T> LazyLinkedListX<T> singleton(T t) {
        List<T> result = ConsList.empty();
        return fromPStack(new DexxListX<>(
                                                        result.prepend(t)),
                                      toPStack());
    }

    public static <T> LazyLinkedListX<T> of(T... t) {

        Builder<T, ConsList<T>> lb = ConsList.<T>factory().newBuilder();
        for (T next : t)
            lb.add(next);
        List<T> vec = lb.build();
        return fromPStack(new DexxListX<>(
                                                        vec),
                                      toPStack());
    }

    public static <T> LazyLinkedListX<T> PStack(List<T> q) {
        return fromPStack(new DexxListX<T>(
                                                         q),
                                      toPStack());
    }
    private static <T> LazyLinkedListX<T> fromPStack(PStack<T> s, Reducer<PStack<T>> pStackReducer) {
        return new LazyLinkedListX<T>(s,null,pStackReducer);
    }


    @SafeVarargs
    public static <T> LazyLinkedListX<T> PStack(T... elements) {
        return fromPStack(of(elements), toPStack());
    }

    @Wither
    private final List<T> list;

    @Override
    public DexxListX<T> plus(T e) {
        return withList(list.prepend(e));
    }

    @Override
    public DexxListX<T> plusAll(Collection<? extends T> l) {
        List<T> vec = list;
        for (T next : l) {
            vec = vec.prepend(next);
        }

        return withList(vec);
    }

    @Override
    public DexxListX<T> with(int i, T e) {
        if (i < 0 || i > size())
            throw new IndexOutOfBoundsException(
                                                "Index " + i + " is out of bounds - size : " + size());
        
        return withList(list.set(i, e));
    }

    @Override
    public DexxListX<T> plus(int i, T e) {
        if (i < 0 || i > size())
            throw new IndexOutOfBoundsException(
                                                "Index " + i + " is out of bounds - size : " + size());
        if (i == 0)
            return withList(list.prepend(e));
        
        if (i == size()) {

            return withList(list.append(e));
        }
 
        return withList(prependAll(list.drop(i),list.take(i).prepend(e)));
       

    }
    private List<T> prependAll(List<T> list,Iterable<T>... its) {
         List<T> result = list;
         for(Iterable<T> toprepend : its) {
             for(T next : toprepend){
                 result = result.prepend(next);
             }
         }
         return result;
    }
    private List<T> prependCol(List<T> list,Collection<T> toprepend) {
        List<T> result = list;
        for(T next : toprepend){
            result = result.prepend(next);
        }
        return result;
   }

    @Override
    public DexxListX<T> plusAll(int i, Collection<? extends T> l) {

        if (i < 0 || i > size())
            throw new IndexOutOfBoundsException(
                                                "Index " + i + " is out of bounds - size : " + size());
        
        return withList(prependAll(list.drop(i),(Collection<T>)l,list.take(i)));
        
    }

    @Override
    public PStack<T> minus(Object e) {

        return fromPStack(this, toPStack())
                          .filter(i -> !Objects.equals(i, e));
    }

    @Override
    public PStack<T> minusAll(Collection<?> list) {
        return (PStack<T>)fromPStack(this, toPStack())
                          .removeAllI((Iterable<T>) list);
    }

    public DexxListX<T> tail() {
        return withList(list.tail());
    }

    public T head() {
        return list.get(0);
    }

    @Override
    public PStack<T> minus(int i) {

        if (i < 0 || i > size())
            throw new IndexOutOfBoundsException(
                                                "Index " + i + " is out of bounds - size : " + size());
        if (i == 0)
            return withList(list.drop(1));
        if (i == size() - 1)
           return fromPStack(this, toPStack()).dropRight(1);
        
        return fromPStack(this,toPStack())
                          .zipWithIndex()
                          .filter(t->t.v2.intValue()!=i)
                          .map(t->t.v1);
      
    }

    @Override
    public DexxListX<T> subList(int start, int end) {

        return withList(list.drop(start)
                            .take(end - start));
    }

    @Override
    public T get(int index) {
        return list.get(index);
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public DexxListX<T> subList(int start) {
        return withList(list.drop(start));
    }

    

}
