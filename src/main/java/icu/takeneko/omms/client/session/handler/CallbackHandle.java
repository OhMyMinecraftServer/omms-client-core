package icu.takeneko.omms.client.session.handler;

public abstract class CallbackHandle<C> {

    private String associateGroupId;

    abstract public void invoke(C context);

    public String getAssociateGroupId() {
        return associateGroupId;
    }

    public void setAssociateGroupId(String associateGroupId) {
        this.associateGroupId = associateGroupId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CallbackHandle)) return false;

        CallbackHandle<?> that = (CallbackHandle<?>) o;

        return getAssociateGroupId() != null ? getAssociateGroupId().equals(that.getAssociateGroupId()) : that.getAssociateGroupId() == null;
    }

    @Override
    public int hashCode() {
        return getAssociateGroupId() != null ? getAssociateGroupId().hashCode() : 0;
    }
}
