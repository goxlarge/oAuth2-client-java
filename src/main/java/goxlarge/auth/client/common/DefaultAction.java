package goxlarge.auth.client.common;

public class DefaultAction extends BaseAction<DefaultAction> {

    public DefaultAction(String action) {
        super(action);
    }

    @Override
    protected DefaultAction getThis() {
        return this;
    }

}