package net.md_5.bungee.module.cmd.bungee;

import net.md_5.bungee.api.plugin.Plugin;

public class PluginBungee extends Plugin
{

    @Override
    public void onEnable()
    {
        getProxy().getPluginManager().registerCommand( this, new CommandBungee( this ) );
    }
}
