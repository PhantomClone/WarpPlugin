package me.phantomclone.warpplugin.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubCommandTest {

    @Mock
    CommandSender mockSender;

    @Mock
    Command mockCommand;

    SubCommand<CommandSender> subCommand;

    @BeforeEach
    void setUp() {
        subCommand = new SubCommand<>() {

            @Override
            public boolean validate(CommandSender sender, String[] args) {
                return args.length > 0;
            }

            @Override
            public void execute(CommandSender sender, String[] args) {
                sender.setOp(true);
            }
        };
    }

    @Test
    void testHasPermission_DefaultMethod() {
        assertTrue(subCommand.hasPermission(mockSender));
    }

    @Test
    void testValidate_WithValidArguments() {
        String[] args = {"arg1"};

        assertTrue(subCommand.validate(mockSender, args));
    }

    @Test
    void testValidate_WithInvalidArguments() {
        String[] args = {};

        assertFalse(subCommand.validate(mockSender, args));
    }

    @Test
    void testExecute() {
        String[] args = {"arg1", "arg2"};

        subCommand.execute(mockSender, args);

        verify(mockSender).setOp(eq(true));
    }

    @Test
    void testOnTabComplete_DefaultMethod() {
        String[] args = {"ar"};

        List<String> completions = subCommand.onTabComplete(mockSender, mockCommand, "label", args);

        assertTrue(completions.isEmpty());
    }
}
