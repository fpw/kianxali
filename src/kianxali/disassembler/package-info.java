/**
 * This package implements a recursive-traversal disassembler. The {@link kianxali.disassembler.Disassembler}
 * gets an {@link ImageFile} and fills a {@link DisassemblyData} instance,
 * informing {@link DisassemblyListener} implementations during the analysis.
 * Information about the discovered entries can be received by {@link kianxali.disassembler.DataListener}
 * implementations that register at the {@link kianxali.disassembler.DisassemblyData}
 * @author fwi
 *
 */
package kianxali.disassembler;

