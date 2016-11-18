package SkipListInterface;

public interface SkipListInterface {
    Integer get(String k);
    boolean add(String k, Integer value);
    boolean remove(String k);
}
