package sai.prasad;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EmailParser {

    public Transaction parse(Email emailBody){
        return new Transaction();
    }
}
