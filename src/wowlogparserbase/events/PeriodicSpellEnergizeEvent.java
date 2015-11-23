
package wowlogparserbase.events;

/**
 *
 * @author racy
 */
public class PeriodicSpellEnergizeEvent extends SpellEnergizeEvent implements PeriodicInterface {

    public PeriodicSpellEnergizeEvent() {
    }

    @Override
    public int parse(String timeDate, String[] values) {
        return super.parse(timeDate, values);
    }

}
