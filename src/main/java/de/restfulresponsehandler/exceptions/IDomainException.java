package de.restfulresponsehandler.exceptions;

/**
 * Created by Armin on 11.08.2015.
 */
public interface IDomainException<T> {

    /**
     * Method to get the response object received from the service.
     * @return The response object of type T.
     */
    public T getResponse();

}
