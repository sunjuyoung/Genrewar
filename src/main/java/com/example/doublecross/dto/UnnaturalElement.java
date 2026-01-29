package com.example.doublecross.dto;

/**
 * 스토리에서 발견된 뜬금없는 요소
 * 
 * @param turn 해당 요소가 등장한 턴
 * @param element 뜬금없는 단어/요소
 * @param reason 뜬금없다고 판단한 이유
 */
public record UnnaturalElement(
    int turn,
    String element,
    String reason
) {}
