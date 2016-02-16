/*
 *
 *  Copyright 2015 Robert Winkler
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package io.github.robwin.markup.builder;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Robert Winkler
 */
public abstract class AbstractMarkupDocBuilder implements MarkupDocBuilder {

    private static final Pattern ANCHOR_UNIGNORABLE_PATTERN = Pattern.compile("[^0-9a-zA-Z-_]+");
    private static final Pattern ANCHOR_IGNORABLE_PATTERN = Pattern.compile("[\\s@#&(){}\\[\\]!$*%+=/:.;,?\\\\<>|]+");
    private static final String ANCHOR_SEPARATION_CHARACTERS = "_-";

    protected StringBuilder documentBuilder = new StringBuilder();
    protected String newLine = System.getProperty("line.separator");
    protected Logger logger = LoggerFactory.getLogger(getClass());

    protected void documentTitle(Markup markup, String title){
        documentBuilder.append(markup).append(title).append(newLine).append(newLine);
    }

    protected void sectionTitleLevel1(Markup markup, String title, String anchor){
        documentBuilder.append(newLine);
        anchor(anchor).newLine();
        documentBuilder.append(markup).append(title).append(newLine);
    }

    @Override
    public MarkupDocBuilder sectionTitleLevel1(String title) {
        return sectionTitleLevel1(title, title);
    }

    protected void sectionTitleLevel2(Markup markup, String title, String anchor){
        documentBuilder.append(newLine);
        anchor(anchor).newLine();
        documentBuilder.append(markup).append(title).append(newLine);
    }

    @Override
    public MarkupDocBuilder sectionTitleLevel2(String title) {
        return sectionTitleLevel2(title, title);
    }

    protected void sectionTitleLevel3(Markup markup, String title, String anchor){
        documentBuilder.append(newLine);
        anchor(anchor).newLine();
        documentBuilder.append(markup).append(title).append(newLine);
    }

    @Override
    public MarkupDocBuilder sectionTitleLevel3(String title) {
        return sectionTitleLevel3(title, title);
    }

    protected void sectionTitleLevel4(Markup markup, String title, String anchor){
        documentBuilder.append(newLine);
        anchor(anchor).newLine();
        documentBuilder.append(markup).append(title).append(newLine);
    }

    @Override
    public MarkupDocBuilder sectionTitleLevel4(String title) {
        return sectionTitleLevel4(title, title);
    }

    @Override
    public MarkupDocBuilder textLine(String text){
        documentBuilder.append(text).append(newLine);
        return this;
    }

    protected void paragraph(Markup markup, String text){
        documentBuilder.append(markup).append(newLine).append(text).append(newLine).append(newLine);
    }

    protected void listing(Markup markup, String text){
        delimitedTextLine(markup, text);
    }

    protected void delimitedTextLine(Markup markup, String text){
        documentBuilder.append(markup).append(newLine).append(text).append(newLine).append(markup).append(newLine).append(newLine);
    }

    protected void delimitedTextLineWithoutLineBreaks(Markup markup, String text){
        documentBuilder.append(markup).append(text).append(markup).append(newLine);
    }

    protected void preserveLineBreaks(Markup markup){
        documentBuilder.append(markup).append(newLine);
    }

    protected void boldTextLine(Markup markup, String text){
        delimitedTextLineWithoutLineBreaks(markup, text);
    }

    protected void italicTextLine(Markup markup, String text){
        delimitedTextLineWithoutLineBreaks(markup, text);
    }

    protected void unorderedList(Markup markup, List<String> list){
        documentBuilder.append(newLine);
        for(String listEntry : list){
            unorderedListItem(markup, listEntry);
        }
        documentBuilder.append(newLine);
    }

    protected void unorderedListItem(Markup markup, String item) {
        documentBuilder.append(markup).append(item).append(newLine);
    }

    @Override
    public MarkupDocBuilder anchor(String anchor, String text) {
        documentBuilder.append(anchorAsString(anchor, text));
        return this;
    }

    @Override
    public MarkupDocBuilder anchor(String anchor) {
        return anchor(anchor, null);
    }

    /**
     * Generic normalization algorithm for all markups (less common denominator character set).
     * Key points :
     * - Anchor is normalized (Normalized.Form.NFD)
     * - Punctuations (excluding [-_]) and spaces are replaced with escape character (depends on markup : Markup.E)
     * - Beginning, ending separation characters [-_] are ignored, repeating separation characters are simplified (keep first one)
     * - Anchor is trimmed and lower cased
     * - If the anchor still contains forbidden characters (non-ASCII, ...), replace the whole anchor with an hash (MD5).
     */
    protected String normalizeAnchor(Markup spaceEscape, String anchor) {
        String normalizedAnchor = anchor.trim();
        normalizedAnchor = Normalizer.normalize(normalizedAnchor, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        normalizedAnchor = ANCHOR_IGNORABLE_PATTERN.matcher(normalizedAnchor).replaceAll(spaceEscape.toString());
        normalizedAnchor = normalizedAnchor.replaceAll(String.format("([%1$s])([%1$s]+)", ANCHOR_SEPARATION_CHARACTERS), "$1");
        normalizedAnchor = StringUtils.strip(normalizedAnchor, ANCHOR_SEPARATION_CHARACTERS);
        normalizedAnchor = normalizedAnchor.trim().toLowerCase();

        String validAnchor = ANCHOR_UNIGNORABLE_PATTERN.matcher(normalizedAnchor).replaceAll("");
        if (validAnchor.length() != normalizedAnchor.length())
            normalizedAnchor = DigestUtils.md5Hex(normalizedAnchor);
        else
            normalizedAnchor = validAnchor;

        return normalizedAnchor;
    }

    @Override
    public MarkupDocBuilder crossReferenceRaw(String document, String anchor, String text) {
        documentBuilder.append(crossReferenceRawAsString(document, anchor, text));
        return this;
    }

    @Override
    public MarkupDocBuilder crossReferenceRaw(String anchor, String text) {
        return crossReferenceRaw(null, anchor, text);
    }

    @Override
    public MarkupDocBuilder crossReferenceRaw(String anchor) {
        return crossReferenceRaw(null, anchor, null);
    }

    @Override
    public MarkupDocBuilder crossReference(String document, String title, String text) {
        documentBuilder.append(crossReferenceAsString(document, title, text));
        return this;
    }

    @Override
    public MarkupDocBuilder crossReference(String title, String text) {
        return crossReference(null, title, text);
    }

    @Override
    public MarkupDocBuilder crossReference(String title) {
        return crossReference(null, title, null);
    }

    @Override
    public MarkupDocBuilder newLine(){
        documentBuilder.append(newLine);
        return this;
    }

    @Override
    public MarkupDocBuilder table(List<List<String>> cells) {
        return tableWithColumnSpecs(null, cells);
    }

    @Override
    public String toString(){
        return documentBuilder.toString();
    }

    protected String addfileExtension(Markup markup, String fileName) {
        return fileName + "." + markup;
    }

    @Override
    public void writeToFileWithoutExtension(String directory, String fileName, Charset charset) throws IOException {
        Files.createDirectories(Paths.get(directory));
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(directory, fileName), charset)){
            writer.write(documentBuilder.toString());
        }
        if (logger.isInfoEnabled()) {
            logger.info("{} was written to: {}", fileName, directory);
        }
        documentBuilder = new StringBuilder();
    }

    @Override
    public void writeToFile(String directory, String fileName, Charset charset) throws IOException {
        writeToFileWithoutExtension(directory, addfileExtension(fileName), charset);
    }
}
