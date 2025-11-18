package edu.univ.erp.support;

import edu.univ.erp.data.auth.AuthRecord;
import edu.univ.erp.data.memory.InMemoryDataStore;

import java.util.NoSuchElementException;

public final class TestData {

    private TestData() {
    }

    public static String userIdForUsername(InMemoryDataStore store, String username) {
        return store.authRecords()
                .values()
                .stream()
                .filter(record -> record.username().equalsIgnoreCase(username))
                .map(AuthRecord::userId)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("User not found: " + username));
    }
}

