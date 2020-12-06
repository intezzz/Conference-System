package presenter;


/**
 * This interface is specifically used to reverse the dependency when the presenter wants to update the
 * view.
 */
public interface IUpdate {
    public void update(String action);
}
