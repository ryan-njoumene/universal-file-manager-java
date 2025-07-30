package com.million_projects.universal_file_manager;

import java.util.Optional;
import java.util.function.Function;

public abstract class Result<T> {
    
        // CONSTRUCTOR
    private Result(){};

        // STATIC METHODS
    // successful result
    public static <T> Result<T> success(T value){
        return new Success<>(value);
    }

    // failed result
    public static <T> Result<T> failure(Throwable error){
        return new Failure<>(error);
    }

    // ABSTRACT METHODS
    public abstract boolean isSuccess();
    public abstract boolean isFailure();

    // optional is used because it accept null as a concrete value.
    // it will only be considered as empty if no value are passed inside
    public abstract Optional<T> getValue();
    public abstract Optional<Throwable> getError();

    /*
     * Function<? super T, ? extends U> mapper (The mapper function):
     * This is a java.util.function.Function interface. It's a functional interface that takes one argument and produces one result.
     * ? super T (Contravariant input): Means the function can accept T or any supertype of T. This makes the map method more flexible.
     * If your Result holds a Cat, you could map it with a function that takes an Animal (a supertype of Cat).
     * ? extends U (Covariant output): Means the function can produce U or any subtype of U. This also adds flexibility.
     * If you're mapping to a Number, the function could return an Integer (a subtype of Number).
     * Purpose: This mapper is the transformation logic. It's the function that will be applied to the successful value of the Result.
     */
    public <U> Result<U> map(Function< ? super T, ? extends  U> mapper){
        if(isSuccess()){
            // If it's a Success, it retrieves the successful value using getValue().get().
            // It then applies the mapper function to this value (mapper.apply(...)).
            // Finally, it wraps the newly transformed value into a new Result.success(U) instance.
            return success( mapper.apply( getValue().get() ));
        }
        return failure( getError().get() ); // Propagate the error to ist caller
        // Instead, it simply retrieves the original error using getError().get()
        // and wraps it into a new Result.failure(U) instance (the U here is just a placeholder, as there's no successful value).
        // The error is "passed through" or "propagated."
    }
     // In essence, Result.map() allows you to chain operations on the value inside a Result without having to manually check for success/failure at each step.
     // If any step in the chain results in a Failure, all subsequent map calls will just propagate that Failure until you explicitly handle it.

    private static final class Success<T> extends Result<T>{
        private final T value;

        private Success(T value){
            this.value = value;
        }

        @Override
        public boolean isSuccess(){ return true; }
        @Override
        public boolean isFailure(){ return false; }

        @Override
        public Optional<T> getValue(){ return Optional.ofNullable(value); } // Handle null success if applicable
        @Override
        public Optional<Throwable> getError(){ return Optional.empty(); }
        
        @Override
        public String toString() { return "Success(" + value + ")"; }
    }

    private static final class Failure<T> extends Result<T>{
        private final Throwable error;

        private Failure(Throwable error){
            this.error = error;
        }

        @Override
        public boolean isSuccess(){ return false; }
        @Override
        public boolean isFailure(){ return true; }

        @Override
        public Optional<T> getValue(){ return Optional.empty(); }
        @Override
        public Optional<Throwable> getError(){ return Optional.of(error); }
        
        @Override
        public String toString() { return "Failure(" + error + ")"; }
    }    


}
