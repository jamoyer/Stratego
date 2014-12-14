package stratego;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.servlet.AsyncContext;

import stratego.model.GameInstance;
import stratego.user.Validator;

/**
 * This class holds and ensures the integrity of the GameInstances and
 * AsyncContexts across all threads.
 * 
 * @author Jacob Moyer
 *
 */
public class AppContext
{
    
    
    private static final int INITIAL_GAMES_CAPACITY = 30;
    private static final HashMap<String, GameInstance> _gameInstances = new HashMap<String, GameInstance>(
            INITIAL_GAMES_CAPACITY);

    private static final int INITIAL_CONTEXT_CAPACITY = 20;
    private static final HashMap<String, AsyncContext> _contextStore = new HashMap<String, AsyncContext>(
            INITIAL_CONTEXT_CAPACITY);
    
    private static final int INITIAL_USER_CAPACITY = 10;
    private static final HashMap<String, Long> _users = new HashMap<String, Long>(
            INITIAL_USER_CAPACITY);

    public AppContext()
    {
    }

    public static void removeGame(final String user)
    {
        synchronized (_gameInstances)
        {
            _gameInstances.remove(user);
        }
    }

    public static void putGame(final String user, final GameInstance game)
    {
        synchronized (_gameInstances)
        {
            _gameInstances.put(user, game);
        }
    }
    
    public static void removeUser(final String user)
    {
        synchronized (_users)
        {
            _users.remove(user);
        }
    }

    public static void putUser(final String user)
    {
        synchronized (_users)
        {
            _users.put(user, Validator.currentTimeSeconds());
        }
    }
    
    public static Long getUser(final String user)
    {
        return _users.get(user);
    }
    
    public static List<String> getOnlineUsers()
    {
        List<String> users = new LinkedList<String>();
        for (String user : _users.keySet())
        {
            users.add(user);
        }
        return users;
    }

    public static GameInstance getGame(final String user)
    {
        return _gameInstances.get(user);
    }

    public static List<GameInstance> getGames()
    {
        List<GameInstance> games = new LinkedList<GameInstance>();
        for (GameInstance game : _gameInstances.values())
        {
            games.add(game);
        }
        return games;
    }

    public static Set<String> getGameUsers()
    {
        Set<String> keys = new HashSet<String>(_gameInstances.keySet().size());
        for (String key : _gameInstances.keySet())
        {
            keys.add(key);
        }
        return keys;
    }

    public static void removeContext(final String user)
    {
        synchronized (_contextStore)
        {
            _contextStore.remove(user);
        }
    }

    public static void putContext(final String user, final AsyncContext context)
    {
        synchronized (_contextStore)
        {
            _contextStore.put(user, context);
        }
    }

    public static List<AsyncContext> getContexts()
    {
        List<AsyncContext> games = new LinkedList<AsyncContext>();
        for (AsyncContext game : _contextStore.values())
        {
            games.add(game);
        }
        return games;
    }

    public static Set<String> getContextUsers()
    {
        Set<String> keys = new HashSet<String>(_contextStore.keySet().size());
        for (String key : _contextStore.keySet())
        {
            keys.add(key);
        }
        return keys;
    }
}
