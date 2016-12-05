package de.esailors.jenkins.teststability;

import hudson.scm.ChangeLogSet;
import hudson.model.User;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Collection;
import hudson.scm.ChangeLogSet.Entry;
import java.util.Iterator;
import hudson.model.Run;
import hudson.scm.RepositoryBrowser;
import java.util.Collections;
import java.net.URL;
import java.io.IOException;
import org.mockito.Mockito;
import org.mockito.Mockito.*;

public class TestChangeLogSet extends ChangeLogSet {

    TestChangeLogSet(Run<?, ?> build) {
        super(build, new RepositoryBrowser<ChangeLogSet.Entry>() {
            @Override
            public URL getChangeSetLink(ChangeLogSet.Entry changeSet) throws IOException {
                return null;
            }
        });
    }

    public boolean isEmptySet() {
        return false;
    }

    public Iterator<Entry> iterator() {
        Collection<Entry> entries = new HashSet<Entry>();
        entries.add(new Entry());
        return entries.iterator();
    }

    public static class Entry extends ChangeLogSet.Entry {

        public String getMsg() {
            return "test message";
        }

        public User getAuthor() {
            User mockUser = Mockito.mock(User.class);
            Mockito.when(mockUser.getId()).thenReturn("testuser");

            return mockUser;
        }

        public Collection<String> getAffectedPaths() {
            return new HashSet<String>();
        }
    }

}