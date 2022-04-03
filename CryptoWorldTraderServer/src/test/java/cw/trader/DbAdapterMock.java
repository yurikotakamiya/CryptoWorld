package cw.trader;

import cwp.db.IDbAdapter;
import cwp.db.IDbEntity;

import java.util.LinkedList;
import java.util.List;

public class DbAdapterMock implements IDbAdapter {
    private List<IDbEntity> written;

    public DbAdapterMock() {
        this.written = new LinkedList<>();
    }

    @Override
    public void write(IDbEntity o) {
        try {
            this.written.add((IDbEntity) o.clone());
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }

    public boolean hasNoNextDbEntity() {
        return this.written.isEmpty();
    }

    public IDbEntity nextDbEntity() {
        return this.written.remove(0);
    }
}
