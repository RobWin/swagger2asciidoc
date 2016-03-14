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
package io.github.robwin.markup.builder.confluence;

import io.github.robwin.markup.builder.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public final class ConfluenceBuilder extends AbstractMarkupDocBuilder {

    public ConfluenceBuilder() {
        super();
    }

    public ConfluenceBuilder(String newLine) {
        super(newLine);
    }

    @Override
    public MarkupDocBuilder copy() {
        return new ConfluenceBuilder(newLine).withAnchorPrefix(anchorPrefix);
    }

    @Override
    public MarkupDocBuilder documentTitle(String title) {
        return this;
    }

    @Override
    public MarkupDocBuilder sectionTitleWithAnchorLevel(int level, String title, String anchor) {
        documentBuilder.append(newLine);
        documentBuilder.append("h").append(level + 1).append(". ").append(title);
        if (isNotBlank(anchor)) {
            documentBuilder.append(" {anchor:").append(anchor).append("}");
        }
        documentBuilder.append(newLine).append(newLine);
        return this;
    }

    @Override
    public MarkupDocBuilder block(String text, MarkupBlockStyle style, String title, MarkupAdmonition admonition) {
        switch (style) {
            case SIDEBAR:
                documentBuilder.append(newLine).append("{quote}").append(newLine);
                if (isNotBlank(title)) {
                    documentBuilder.append(title).append(" \\\\ ");
                }
                documentBuilder.append(text);
                documentBuilder.append(newLine).append("{quote}").append(newLine);
                break;
            case EXAMPLE:
            case LITERAL:
                documentBuilder.append(newLine);
                if (isBlank(title)) {
                    documentBuilder.append("{panel}");
                } else {
                    documentBuilder.append("{panel:title=").append(title).append("}");
                }
                documentBuilder.append(newLine);
                documentBuilder.append(text);
                documentBuilder.append(newLine).append("{panel}").append(newLine);
                break;
            case LISTING:
                documentBuilder.append(newLine);
                if (isBlank(title)) {
                    documentBuilder.append("{code}");
                } else {
                    documentBuilder.append("{code:title=").append(title).append("}");
                }
                documentBuilder.append(newLine);
                documentBuilder.append(text);
                documentBuilder.append(newLine).append("{code}").append(newLine);
                break;
            case PASSTHROUGH:
                documentBuilder.append(newLine).append("{noformat}").append(newLine);
                if (isNotBlank(title)) {
                    documentBuilder.append(title);
                    documentBuilder.append(newLine);
                }
                documentBuilder.append(text);
                documentBuilder.append(newLine).append("{noformat}").append(newLine);
                break;
        }
        return this;
    }

    @Override
    public MarkupDocBuilder listing(String text, String language) {
        documentBuilder.append(newLine);
        if (isBlank(language)) {
            documentBuilder.append("{code}");
        } else {
            documentBuilder.append("{code:language=").append(language).append("}");
        }
        documentBuilder.append(newLine);
        documentBuilder.append(text);
        documentBuilder.append(newLine).append("{code}").append(newLine).append(newLine);
        return this;
    }

    @Override
    public MarkupDocBuilder boldText(String text) {
        documentBuilder.append("*").append(text).append("*");
        return this;
    }

    @Override
    public MarkupDocBuilder italicText(String text) {
        documentBuilder.append("_").append(text).append("_");
        return this;
    }

    @Override
    public MarkupDocBuilder unorderedList(List<String> list) {
        documentBuilder.append(newLine).append(newLine);
        for (String item : list) {
            documentBuilder.append("* ").append(item).append(newLine);
        }
        documentBuilder.append(newLine);
        return this;
    }

    @Override
    public MarkupDocBuilder unorderedListItem(String item) {
        documentBuilder.append("* ").append(item);
        return this;
    }

    @Override
    public MarkupDocBuilder tableWithHeaderRow(List<String> rowsInPSV) {
        return this;
    }

    @Override
    public MarkupDocBuilder tableWithColumnSpecs(List<MarkupTableColumn> columnSpecs, List<List<String>> cells) {
        documentBuilder.append(newLine);
        if (columnSpecs != null && !columnSpecs.isEmpty()) {
            documentBuilder.append("||");
            for (MarkupTableColumn column : columnSpecs) {
                documentBuilder.append(escapeCellContent(column.header)).append("||");
            }
            documentBuilder.append(newLine);
        }
        if (cells != null) {
            for (List<String> row : cells) {
                documentBuilder.append("|");
                for (String cell : row) {
                    documentBuilder.append(escapeCellContent(cell)).append("|");
                }
                documentBuilder.append(newLine);
            }
        }
        return this;
    }

    private String escapeCellContent(String content) {
        if (content == null) {
            return " ";
        }
        return content.replace("|", "\\|").replace(newLine, "\\\\");
    }

    private String normalizeAnchor(String anchor) {
        return normalizeAnchor(Confluence.SPACE_ESCAPE, anchor);
    }


    @Override
    public MarkupDocBuilder anchor(String anchor, String text) {
        documentBuilder.append("{anchor:").append(normalizeAnchor(anchor)).append("}");
        return this;
    }

    @Override
    public MarkupDocBuilder crossReference(String document, String anchor, String text) {
        crossReferenceRaw(document, anchor, text);
        return this;
    }

    @Override
    public MarkupDocBuilder crossReferenceRaw(String document, String anchor, String text) {
        documentBuilder.append("[");
        if (isNotBlank(document)) {
            documentBuilder.append(document);
        }
        documentBuilder.append("#").append(normalizeAnchor(anchor));
        if (isNotBlank(text)) {
            documentBuilder.append("|").append(text);
        }
        documentBuilder.append("]");
        return this;
    }

    @Override
    public MarkupDocBuilder newLine(boolean forceLineBreak) {
        documentBuilder.append(" \\\\ ").append(newLine);
        return this;
    }

    @Override
    public MarkupDocBuilder importMarkup(Reader markupText, int levelOffset) throws IOException {
        final BufferedReader reader = new BufferedReader(markupText);
        String line;
        while ((line = reader.readLine()) != null) {
            documentBuilder.append(line);
        }
        return this;
    }

    @Override
    public String addFileExtension(String fileName) {
        return addFileExtension(Confluence.FILE_EXTENSION, fileName);
    }
}
