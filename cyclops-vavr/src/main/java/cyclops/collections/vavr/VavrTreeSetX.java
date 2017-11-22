package cyclops.collections.vavr;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import com.oath.cyclops.data.collections.extensions.CollectionX;
import com.oath.cyclops.data.collections.extensions.lazy.immutable.LazyPOrderedSetX;
import com.oath.cyclops.types.Unwrapable;
import com.oath.cyclops.types.foldable.Evaluation;
import com.oath.cyclops.types.persistent.PersistentSortedSet;
import cyclops.collections.immutable.OrderedSetX;
import cyclops.collections.immutable.VectorX;
import cyclops.control.Option;
import cyclops.function.Reducer;
import cyclops.reactive.ReactiveSeq;
import cyclops.data.tuple.Tuple2;



import io.vavr.collection.SortedSet;
import io.vavr.collection.TreeSet;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;


@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class VavrTreeSetX<T> implements PersistentSortedSet<T>, Unwrapable {

    public static <T> OrderedSetX<T> treeSetX(ReactiveSeq<T> stream, Comparator<? super T> c){
        return fromStream(stream,c);
    }
    public static <T extends Comparable<? super T>> OrderedSetX<T> treeSetX(ReactiveSeq<T> stream){
        return fromStream(stream);
    }
    public static <T> OrderedSetX<T> copyFromCollection(CollectionX<T> vec, Comparator<T> comp) {

        return VavrTreeSetX.empty(comp)
                .plusAll(vec);

    }

    @Override
    public <R> R unwrap() {
        return (R)set;
    }

    /**
     * Create a LazyPOrderedSetX from a Stream
     *
     * @param stream to construct a LazyQueueX from
     * @return LazyPOrderedSetX
     */
    public static <T extends Comparable<? super T>> LazyPOrderedSetX<T> fromStream(Stream<T> stream) {
        return new LazyPOrderedSetX<T>(null,ReactiveSeq.fromStream(stream), toPersistentSortedSet(), Evaluation.LAZY);
    }
    public static <T> LazyPOrderedSetX<T> fromStream(Stream<T> stream,Comparator<? super T> c) {
        return new LazyPOrderedSetX<T>(null,ReactiveSeq.fromStream(stream), toPersistentSortedSet(c), Evaluation.LAZY);
    }

    /**
     * Create a LazyPOrderedSetX that contains the Integers between start and end
     *
     * @param start
     *            Number of range to start from
     * @param end
     *            Number for range to end at
     * @return Range ListX
     */
    public static LazyPOrderedSetX<Integer> range(int start, int end) {
        return fromStream(ReactiveSeq.range(start, end));
    }

    /**
     * Create a LazyPOrderedSetX that contains the Longs between start and end
     *
     * @param start
     *            Number of range to start from
     * @param end
     *            Number for range to end at
     * @return Range ListX
     */
    public static LazyPOrderedSetX<Long> rangeLong(long start, long end) {
        return fromStream(ReactiveSeq.rangeLong(start, end));
    }

    /**
     * Unfold a function into a ListX
     *
     * <pre>
     * {@code
     *  LazyPOrderedSetX.unfold(1,i->i<=6 ? Optional.of(Tuple.tuple(i,i+1)) : Optional.empty());
     *
     * //(1,2,3,4,5)
     *
     * }</pre>
     *
     * @param seed Initial value
     * @param unfolder Iteratively applied function, terminated by an empty Optional
     * @return ListX generated by unfolder function
     */
    public static <U, T extends Comparable<? super T>> LazyPOrderedSetX<T> unfold(U seed, Function<? super U, Option<Tuple2<T, U>>> unfolder) {
        return fromStream(ReactiveSeq.unfold(seed, unfolder));
    }

    /**
     * Generate a LazyPOrderedSetX from the provided Supplier up to the provided limit number of times
     *
     * @param limit Max number of elements to generate
     * @param s Supplier to generate ListX elements
     * @return ListX generated from the provided Supplier
     */
    public static <T extends Comparable<? super T>> LazyPOrderedSetX<T> generate(long limit, Supplier<T> s) {

        return fromStream(ReactiveSeq.generate(s)
                                     .limit(limit));
    }

    /**
     * Create a LazyPOrderedSetX by iterative application of a function to an initial element up to the supplied limit number of times
     *
     * @param limit Max number of elements to generate
     * @param seed Initial element
     * @param f Iteratively applied to each element to generate the next element
     * @return ListX generated by iterative application
     */
    public static <T extends Comparable<? super T>> LazyPOrderedSetX<T> iterate(long limit, final T seed, final UnaryOperator<T> f) {
        return fromStream(ReactiveSeq.iterate(seed, f)
                                     .limit(limit));
    }

    /**
     * <pre>
     * {@code
     * PersistentSortedSet<Integer> q = VavrTreeSetX.<Integer>toPersistentSortedSet()
                                      .mapReduce(Stream.of(1,2,3,4));
     *
     * }
     * </pre>
     * @return Reducer for PersistentSortedSet
     */
    public static <T extends Comparable<? super T>> Reducer<PersistentSortedSet<T>,T> toPersistentSortedSet() {
        return Reducer.<PersistentSortedSet<T>,T> of(VavrTreeSetX.emptyPersistentSortedSet(), (final PersistentSortedSet<T> a) -> b -> a.plusAll(b),
                                           (final T x) -> VavrTreeSetX.singleton(x));
    }
    public static <T> Reducer<PersistentSortedSet<T>,T> toPersistentSortedSet(Comparator<? super T> comparator) {
        return Reducer.<PersistentSortedSet<T>,T> of(VavrTreeSetX.emptyPersistentSortedSet(comparator), (final PersistentSortedSet<T> a) -> b -> a.plusAll(b),
                                           (final T x) -> VavrTreeSetX.singleton(comparator,x));
    }
    public static <T extends Comparable<? super T>> VavrTreeSetX<T> emptyPersistentSortedSet() {
        return new VavrTreeSetX<T>(TreeSet.empty());
    }
    public static <T> VavrTreeSetX<T> emptyPersistentSortedSet(Comparator<? super T> comparator) {
        return new VavrTreeSetX<T>(TreeSet.empty(comparator));
    }
    public static <T extends Comparable<? super T>> LazyPOrderedSetX<T> empty() {
        return fromPersistentSortedSet(new VavrTreeSetX<T>(
                TreeSet.empty()),toPersistentSortedSet());
    }
    private static <T> LazyPOrderedSetX<T> fromPersistentSortedSet(PersistentSortedSet<T> ordered, Reducer<PersistentSortedSet<T>,T> reducer) {
        return  new LazyPOrderedSetX<T>(ordered,null,reducer, Evaluation.LAZY);
    }
    public static <T> LazyPOrderedSetX<T> empty(Comparator<? super T> comparator) {
        return fromPersistentSortedSet(new VavrTreeSetX<T>(
                TreeSet.empty(comparator)),toPersistentSortedSet(comparator));
    }
    public static <T extends Comparable<? super T>> LazyPOrderedSetX<T> singleton(T t) {
        return fromPersistentSortedSet(new VavrTreeSetX<>(
                TreeSet.of(t)),toPersistentSortedSet());
    }
    public static <T>  LazyPOrderedSetX<T> singleton(Comparator<? super T> comparator,T t) {
        return fromPersistentSortedSet(new VavrTreeSetX<T>(
                TreeSet.of(comparator,t)),toPersistentSortedSet(comparator));

    }

    public static <T extends Comparable<? super T>> LazyPOrderedSetX<T> of(T... t) {
        return fromPersistentSortedSet(new VavrTreeSetX<>(
                                        TreeSet.of(t)),toPersistentSortedSet());
    }

    public static <T> LazyPOrderedSetX<T> ofAll(SortedSet<T> set) {
        return fromPersistentSortedSet(new VavrTreeSetX<>(set), toPersistentSortedSet(set.comparator()));
    }
    public static <T> LazyPOrderedSetX<T> PersistentSortedSet(SortedSet<T> set) {
        return fromPersistentSortedSet(new VavrTreeSetX<>(set), toPersistentSortedSet(set.comparator()));
    }

    public static <T extends Comparable<? super T>> LazyPOrderedSetX<T> PersistentSortedSet(T... elements) {
        return fromPersistentSortedSet(of(elements), toPersistentSortedSet());
    }

    @Wither
    private final SortedSet<T> set;

    @Override
    public VavrTreeSetX<T> plus(T e) {
        return withSet(set.add(e));
    }

  @Override
  public VavrTreeSetX<T> plusAll(Iterable<? extends T> list) {
    return withSet(set.addAll(list));
  }

  @Override
  public VavrTreeSetX<T> removeValue(T e) {
    return withSet(set.remove(e));
  }

  @Override
  public VavrTreeSetX<T> removeAll(Iterable<? extends T> list) {
    return withSet(set.removeAll(list));
  }


  @Override
    public int size() {
        return set.size();
    }

    @Override
    public Iterator<T> iterator() {
        return set.iterator();
    }

    @Override
    public Option<T> get(int index) {
        if (index > set.size() || index < 0)
            return Option.none();

        T result = null;
        Iterator<T> it = set.iterator();
        for (int i = 0; i < index; i++) {
            result = it.next();
        }
        return Option.some(result);
    }


    public int indexOf(T o) {
        return set.toStream()
                  .zipWithIndex()
                  .find(t -> t._1.equals(o))
                  .map(t -> t._2)
                  .getOrElse(-1)
                  .intValue();
    }

}
