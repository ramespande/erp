package edu.univ.erp.data.memory;

import edu.univ.erp.data.auth.AuthRecord;
import edu.univ.erp.data.auth.AuthRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class InMemoryAuthRepository implements AuthRepository {

    private final InMemoryDataStore store;

    public InMemoryAuthRepository(InMemoryDataStore store) {
        this.store = store;
    }

    @Override
    public Optional<AuthRecord> findByUsername(String username) {
        return store.authRecords()
                .values()
                .stream()
                .filter(record -> record.username().equalsIgnoreCase(username))
                .findFirst();
    }

    @Override
    public Optional<AuthRecord> findByUserId(String userId) {
        return Optional.ofNullable(store.authRecords().get(userId));
    }

    @Override
    public void save(AuthRecord record) {
        store.authRecords().put(record.userId(), record);
        store.save();
    }

    @Override
    public List<AuthRecord> findAll() {
        return new ArrayList<>(store.authRecords().values());
    }
}

