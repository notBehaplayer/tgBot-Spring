package com.example.telegram_bot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class MyTelegramBot extends TelegramLongPollingBot {

    private final UserRepository userRepository;

    @Autowired
    public MyTelegramBot(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public String getBotUsername() {
        return "bpsExampleBot";
    }

    @Override
    public String getBotToken() {
        return "7342253452:AAERN4KtHZDQwgex2MTEcd4QVoo-FGBCLns";
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            if (messageText.startsWith("/signUp")) {
                handleSignUp(messageText, chatId);
            } else if (messageText.startsWith("/signIn")) {
                handleSignIn(messageText, chatId);
            } else if (messageText.equals("/profile")) {
                handleProfile(chatId);
            }
        }
    }

    private void handleSignUp(String messageText, long chatId) {
        String[] parts = messageText.split(" ");
        if (parts.length < 3) {
            sendMessage(chatId, "Usage: /signUp <username> <password>");
            return;
        }

        String username = parts[1];
        String password = parts[2];

        if (userRepository.findByUsername(username) != null) {
            sendMessage(chatId, "This username is already taken.");
            return;
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password); // Здесь лучше хешировать пароль
        user.setChatId(String.valueOf(chatId));
        userRepository.save(user);

        sendMessage(chatId, "Registration successful!");
    }

    private void handleSignIn(String messageText, long chatId) {
        String[] parts = messageText.split(" ");
        if (parts.length < 3) {
            sendMessage(chatId, "Usage: /signIn <username> <password>");
            return;
        }

        String username = parts[1];
        String password = parts[2];
        User user = userRepository.findByUsername(username);

        if (user == null || !user.getPassword().equals(password)) {
            sendMessage(chatId, "Invalid username or password.");
            return;
        }

        sendMessage(chatId, "Sign-in successful!");
    }

    private void handleProfile(long chatId) {
        User user = userRepository.findByUsername(String.valueOf(chatId));

        if (user == null) {
            sendMessage(chatId, "You are not signed in. Please use /signIn first.");
        } else {
            sendMessage(chatId, "Profile:\nUsername: " + user.getUsername());
        }
    }

    private void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
