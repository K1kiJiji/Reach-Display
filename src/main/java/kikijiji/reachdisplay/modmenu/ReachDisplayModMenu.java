package kikijiji.reachdisplay.modmenu;


import com.terraformersmc.modmenu.api.ModMenuApi;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import kikijiji.reachdisplay.config.ReachDisplayConfigScreen;


public class ReachDisplayModMenu implements ModMenuApi
{
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory()
    {
        return ReachDisplayConfigScreen::new;
    }
}